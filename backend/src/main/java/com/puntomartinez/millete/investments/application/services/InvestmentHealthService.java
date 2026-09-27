package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases.HealthIssue;
import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases.HealthReport;
import com.puntomartinez.millete.investments.domain.ports.out.*;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/** Reconciles actionable portfolio integrity and market-data warnings. */
@Service
public class InvestmentHealthService {
    private final AssetRepository assets;
    private final ActivityRepository activities;
    private final HoldingRepository holdings;
    private final MarketDataRepository marketData;
    private final UserCurrencyPort currencies;
    private final DailyTransferPort transfers;
    private final InvestmentHealthNotificationPort healthNotifications;
    private final TimeProvider time;
    private final Duration maximumMarketDataAge;

    public InvestmentHealthService(AssetRepository assets, ActivityRepository activities,
                                   HoldingRepository holdings, MarketDataRepository marketData,
                                   UserCurrencyPort currencies, DailyTransferPort transfers,
                                   InvestmentHealthNotificationPort healthNotifications, TimeProvider time,
                                   @Value("${app.investments.market-data.max-age:PT72H}") Duration maximumMarketDataAge) {
        this.assets = assets;
        this.activities = activities;
        this.holdings = holdings;
        this.marketData = marketData;
        this.currencies = currencies;
        this.transfers = transfers;
        this.healthNotifications = healthNotifications;
        this.time = time;
        this.maximumMarketDataAge = maximumMarketDataAge;
    }

    @Transactional
    public HealthReport check(UUID userId) {
        Instant now = time.now();
        activities.lockUserPortfolio(userId);
        List<HealthIssue> issues = new ArrayList<>();
        List<Asset> userAssets = assets.findAssetsByUserId(userId, true);
        List<Activity> userActivities = activities.findActivitiesByUserId(userId);
        List<Holding> userHoldings = holdings.findHoldingsByUserId(userId);
        String localCurrency = currencies.currencyAt(userId, now).map(UserLocalCurrencyPeriod::currency)
                .or(() -> currencies.currentCurrency(userId)).orElse(null);

        Map<UUID, Asset> assetById = userAssets.stream()
                .collect(Collectors.toMap(Asset::getId, asset -> asset));
        for (Activity activity : userActivities) {
            if (activity.getAssetId() != null && !assetById.containsKey(activity.getAssetId())) {
                add(issues, "ASSET_REFERENCE_MISSING", activity.getAssetId().toString(), "error",
                        "La operación " + activity.getId() + " apunta a un activo que no existe en la cartera.");
            }
        }
        for (Holding holding : userHoldings) {
            if (!assetById.containsKey(holding.assetId())) {
                add(issues, "ASSET_REFERENCE_MISSING", holding.assetId().toString(), "error",
                        "La posición inicial " + holding.id() + " apunta a un activo que no existe en la cartera.");
            }
        }

        if (localCurrency == null) {
            add(issues, "LOCAL_CURRENCY_MISSING", userId.toString(), "warning",
                    "Configura una moneda local para valorar la cartera.");
        }

        for (Asset asset : userAssets) {
            if (asset.isActive() && asset.getSectorId() == null) {
                add(issues, "SECTOR_MISSING", asset.getId().toString(), "warning",
                        "El activo «" + asset.getName() + "» no tiene sector asignado.");
            } else if (asset.isActive() && !assets.sectorExists(asset.getSectorId())) {
                add(issues, "SECTOR_UNAVAILABLE", asset.getId().toString(), "warning",
                        "El sector asignado a «" + asset.getName() + "» ya no está disponible.");
            }
        }

        PortfolioReplay.Result replay = null;
        try {
            replay = PortfolioReplay.replay(userId, userActivities, userHoldings);
        } catch (PortfolioReplay.LedgerIntegrityException exception) {
            add(issues, exception.code(), exception.resourceId(), "error", exception.getMessage());
            if ("CASH_NEGATIVE".equals(exception.code())) {
                checkFx(issues, userId, exception.resourceId(), localCurrency, now, exception.resourceId());
            } else if ("POSITION_NEGATIVE".equals(exception.code())) {
                Asset asset = assetById.get(UUID.fromString(exception.resourceId()));
                if (asset != null && asset.isActive()) checkMarketData(issues, userId, asset, localCurrency, now);
            }
        } catch (RuntimeException exception) {
            add(issues, "LEDGER_INVALID", userId.toString(), "error",
                    "El libro de inversiones no se puede reconstruir: " + safeMessage(exception));
        }

        Set<UUID> openAssets = replay == null ? Set.of() : replay.lots().stream()
                .filter(lot -> lot.getRemainingQuantity().signum() > 0)
                .map(Lot::getAssetId).collect(Collectors.toSet());
        for (UUID assetId : openAssets) {
            Asset asset = assetById.get(assetId);
            if (asset == null) {
                continue;
            }
            if (!asset.isActive()) continue;
            checkMarketData(issues, userId, asset, localCurrency, now);
        }

        if (replay != null) {
            replay.cashBalances().forEach((currency, amount) -> {
                if (amount.signum() < 0) {
                    add(issues, "CASH_NEGATIVE", currency, "error", "El saldo de efectivo " + currency + " es negativo.");
                }
                checkFx(issues, userId, currency, localCurrency, now, currency);
            });
        }

        for (Activity activity : userActivities) {
            if (activity.getType() != ActivityType.DEPOSIT && activity.getType() != ActivityType.WITHDRAW) continue;
            DailyTransferPort.TransferDirection direction = activity.getType() == ActivityType.DEPOSIT
                    ? DailyTransferPort.TransferDirection.TRANSFER_OUT
                    : DailyTransferPort.TransferDirection.TRANSFER_IN;
            UUID transactionId = activity.getLinkedTransactionId();
            boolean synchronizedTransfer = transactionId != null && transfers.matchesTransfer(
                    userId, activity.getId(), transactionId, direction,
                    activity.getAmountInLocal(), activity.getLocalCurrency());
            if (!synchronizedTransfer) {
                add(issues, "TRANSFER_OUT_OF_SYNC", activity.getId().toString(), "error",
                        "La transferencia diaria vinculada a esta operación no coincide o no existe.");
            }
        }

        Map<String, HealthIssue> unique = new LinkedHashMap<>();
        issues.forEach(issue -> unique.put(issue.key(), issue));
        List<HealthIssue> reportIssues = new ArrayList<>(unique.values());
        reportIssues.sort(Comparator.comparing(HealthIssue::code).thenComparing(HealthIssue::resourceId));
        healthNotifications.reconcile(userId, reportIssues);
        return new HealthReport(now, List.copyOf(reportIssues));
    }

    private void checkMarketData(List<HealthIssue> issues, UUID userId, Asset asset,
                                 String localCurrency, Instant now) {
        Optional<AssetPrice> quote = marketData.latestPriceAt(userId, asset.getId(), now);
        if (quote.isEmpty()) {
            add(issues, "PRICE_MISSING", asset.getId().toString(), "warning",
                    "El activo «" + asset.getName() + "» no tiene un precio de cierre disponible.");
        } else if (isStale(quote.get().timestamp(), now)) {
            add(issues, "PRICE_STALE", asset.getId().toString(), "warning",
                    "El último precio de «" + asset.getName() + "» supera la antigüedad configurada.");
        }
        checkFx(issues, userId, asset.getCurrency(), localCurrency, now, asset.getId().toString());
    }

    private void checkFx(List<HealthIssue> issues, UUID userId, String currency, String localCurrency,
                         Instant now, String resourceId) {
        if (currency == null || localCurrency == null || currency.equalsIgnoreCase(localCurrency)) return;
        Optional<FxRate> rate = marketData.latestFxAt(userId, currency, localCurrency, now)
                .or(() -> marketData.latestFxAt(userId, localCurrency, currency, now));
        if (rate.isEmpty()) {
            add(issues, "FX_MISSING", resourceId, "warning",
                    "Falta un tipo de cambio para convertir " + currency + " a " + localCurrency + ".");
        } else if (isStale(rate.get().timestamp(), now)) {
            add(issues, "FX_STALE", resourceId, "warning",
                    "El tipo de cambio de " + currency + " a " + localCurrency + " supera la antigüedad configurada.");
        }
    }

    private boolean isStale(Instant timestamp, Instant now) {
        return timestamp.plus(maximumMarketDataAge).isBefore(now);
    }

    private void add(List<HealthIssue> issues, String code, String resourceId,
                     String severity, String message) {
        issues.add(new HealthIssue(code + ":" + resourceId, code, resourceId, severity, message));
    }

    private String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) return "estado inconsistente";
        return message.length() > 180 ? message.substring(0, 180) : message;
    }
}
