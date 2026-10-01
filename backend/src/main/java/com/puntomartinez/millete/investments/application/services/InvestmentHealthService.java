package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.PortfolioReplay;
import com.puntomartinez.millete.investments.domain.model.Position;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.CheckInvestmentHealthUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.HoldingRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentHealthNotificationPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentTrackingSettingsRepository;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.TransactionIntegrationPort;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Reconciles investment portfolio integrity and market-data warnings.
 */
@Service
public class InvestmentHealthService
        implements CheckInvestmentHealthUseCase {

    private final ActivityRepository activities;
    private final HoldingRepository holdings;
    private final InvestmentTrackingSettingsRepository trackingSettings;
    private final SharedAssetRepository sharedAssets;
    private final UserAssetRepository userAssets;
    private final AssetPriceRepository assetPrices;
    private final UserAssetPriceRepository userAssetPrices;
    private final FxRateRepository fxRates;
    private final UserCurrencyPort userCurrencies;
    private final TransactionIntegrationPort transactions;
    private final InvestmentHealthNotificationPort healthNotifications;
    private final TimeProvider time;
    private final Duration maximumMarketDataAge;

    public InvestmentHealthService(
            ActivityRepository activities,
            HoldingRepository holdings,
            InvestmentTrackingSettingsRepository trackingSettings,
            SharedAssetRepository sharedAssets,
            UserAssetRepository userAssets,
            AssetPriceRepository assetPrices,
            UserAssetPriceRepository userAssetPrices,
            FxRateRepository fxRates,
            UserCurrencyPort userCurrencies,
            TransactionIntegrationPort transactions,
            InvestmentHealthNotificationPort healthNotifications,
            TimeProvider time,
            @Value("${app.investments.market-data.max-age:PT72H}")
            Duration maximumMarketDataAge
    ) {
        this.activities = activities;
        this.holdings = holdings;
        this.trackingSettings = trackingSettings;
        this.sharedAssets = sharedAssets;
        this.userAssets = userAssets;
        this.assetPrices = assetPrices;
        this.userAssetPrices = userAssetPrices;
        this.fxRates = fxRates;
        this.userCurrencies = userCurrencies;
        this.transactions = transactions;
        this.healthNotifications = healthNotifications;
        this.time = time;
        this.maximumMarketDataAge = maximumMarketDataAge;
    }

    @Override
    @Transactional
    public HealthReport check(UUID userId) {
        validateUserId(userId);

        Instant now =
                time.now();

        List<HealthIssue> issues =
                new ArrayList<>();

        Optional<Instant> trackingStart =
                trackingSettings.findTrackingStartAt(userId);

        if (trackingStart.isEmpty()) {
            add(
                    issues,
                    "TRACKING_NOT_CONFIGURED",
                    userId.toString(),
                    "error",
                    "El tracking de Investments no está configurado."
            );
        }

        CurrencyCode localCurrency =
                userCurrencies
                        .currencyAt(
                                userId,
                                now
                        )
                        .orElse(null);

        if (localCurrency == null) {
            add(
                    issues,
                    "LOCAL_CURRENCY_MISSING",
                    userId.toString(),
                    "warning",
                    "Configura una moneda local para valorar la cartera."
            );
        }

        List<Activity> userActivities =
                activities.findAllByUserId(userId);

        List<Holding> userHoldings =
                holdings.findAllByUserId(userId);

        checkActivityAssetReferences(
                userId,
                userActivities,
                issues
        );

        checkHoldingAssetReferences(
                userId,
                userHoldings,
                issues
        );

        checkAssetSectors(
                userId,
                userActivities,
                userHoldings,
                issues
        );

        PortfolioReplay.Result replay =
                replayPortfolio(
                        userId,
                        userActivities,
                        userHoldings,
                        trackingStart,
                        issues
                );

        if (replay != null) {
            checkPositions(
                    userId,
                    replay.positions(),
                    localCurrency,
                    now,
                    issues
            );

            checkCash(
                    replay.cashBalances(),
                    localCurrency,
                    now,
                    issues
            );
        }

        checkTransfers(
                userId,
                userActivities,
                issues
        );

        Map<String, HealthIssue> unique =
                new LinkedHashMap<>();

        for (HealthIssue issue : issues) {
            unique.put(
                    issueKey(issue),
                    issue
            );
        }

        List<HealthIssue> reportIssues =
                new ArrayList<>(
                        unique.values()
                );

        reportIssues.sort(
                Comparator
                        .comparing(HealthIssue::code)
                        .thenComparing(
                                HealthIssue::resourceId,
                                Comparator.nullsFirst(
                                        Comparator.naturalOrder()
                                )
                        )
        );

        healthNotifications.reconcile(
                userId,
                List.copyOf(reportIssues)
        );

        return new HealthReport(
                now,
                List.copyOf(reportIssues)
        );
    }

    private PortfolioReplay.Result replayPortfolio(
            UUID userId,
            List<Activity> userActivities,
            List<Holding> userHoldings,
            Optional<Instant> trackingStart,
            List<HealthIssue> issues
    ) {
        if (trackingStart.isEmpty()) {
            return null;
        }

        try {
            return PortfolioReplay.replay(
                    userId,
                    userActivities,
                    userHoldings,
                    trackingStart.get()
            );

        } catch (PortfolioReplay.LedgerIntegrityException exception) {

            add(
                    issues,
                    exception.code(),
                    exception.resourceId(),
                    "error",
                    exception.getMessage()
            );

            return null;

        } catch (RuntimeException exception) {

            add(
                    issues,
                    "LEDGER_INVALID",
                    userId.toString(),
                    "error",
                    "El libro de inversiones no se puede reconstruir: "
                            + safeMessage(exception)
            );

            return null;
        }
    }

    private void checkActivityAssetReferences(
            UUID userId,
            List<Activity> userActivities,
            List<HealthIssue> issues
    ) {
        for (Activity activity : userActivities) {
            AssetReference reference =
                    activity.getAssetReference();

            if (reference == null) {
                continue;
            }

            if (!assetExists(
                    userId,
                    reference
            )) {
                add(
                        issues,
                        "ASSET_REFERENCE_MISSING",
                        reference.id().toString(),
                        "error",
                        "La operación "
                                + activity.getId()
                                + " apunta a un activo que no existe "
                                + "o no pertenece al usuario."
                );
            }
        }
    }

    private void checkHoldingAssetReferences(
            UUID userId,
            List<Holding> userHoldings,
            List<HealthIssue> issues
    ) {
        for (Holding holding : userHoldings) {
            AssetReference reference =
                    holding.getAssetReference();

            if (!assetExists(
                    userId,
                    reference
            )) {
                add(
                        issues,
                        "ASSET_REFERENCE_MISSING",
                        reference.id().toString(),
                        "error",
                        "El holding "
                                + holding.getId()
                                + " apunta a un activo que no existe "
                                + "o no pertenece al usuario."
                );
            }
        }
    }

    private boolean assetExists(
            UUID userId,
            AssetReference reference
    ) {
        if (reference == null) {
            return false;
        }

        return switch (reference.kind()) {
            case SHARED ->
                    sharedAssets
                            .findById(
                                    reference.id()
                            )
                            .isPresent();

            case USER ->
                    userAssets
                            .findByIdAndUserId(
                                    reference.id(),
                                    userId
                            )
                            .isPresent();
        };
    }

    private void checkAssetSectors(
            UUID userId,
            List<Activity> userActivities,
            List<Holding> userHoldings,
            List<HealthIssue> issues
    ) {
        Map<AssetReference, Boolean> references =
                new LinkedHashMap<>();

        for (Activity activity : userActivities) {
            if (activity.getAssetReference() != null) {
                references.put(
                        activity.getAssetReference(),
                        Boolean.TRUE
                );
            }
        }

        for (Holding holding : userHoldings) {
            references.put(
                    holding.getAssetReference(),
                    Boolean.TRUE
            );
        }

        for (AssetReference reference :
                references.keySet()) {

            if (reference == null) {
                continue;
            }

            switch (reference.kind()) {
                case SHARED -> {
                    Optional<SharedAsset> asset =
                            sharedAssets.findById(
                                    reference.id()
                            );

                    if (asset.isPresent()
                            && asset.get().getSector() == null) {

                        add(
                                issues,
                                "SECTOR_MISSING",
                                reference.id().toString(),
                                "warning",
                                "El activo «"
                                        + asset.get().getName()
                                        + "» no tiene sector asignado."
                        );
                    }
                }

                case USER -> {
                    Optional<UserAsset> asset =
                            userAssets.findByIdAndUserId(
                                    reference.id(),
                                    userId
                            );

                    if (asset.isPresent()
                            && asset.get().getSector() == null) {

                        add(
                                issues,
                                "SECTOR_MISSING",
                                reference.id().toString(),
                                "warning",
                                "El activo «"
                                        + asset.get().getName()
                                        + "» no tiene sector asignado."
                        );
                    }
                }
            }
        }
    }

    private void checkPositions(
            UUID userId,
            List<Position> positions,
            CurrencyCode localCurrency,
            Instant now,
            List<HealthIssue> issues
    ) {
        for (Position position : positions) {
            if (position.quantity().signum() <= 0) {
                continue;
            }

            checkMarketData(
                    userId,
                    position.assetReference(),
                    localCurrency,
                    now,
                    issues
            );
        }
    }

    private void checkCash(
            Map<CurrencyCode, Money> cashBalances,
            CurrencyCode localCurrency,
            Instant now,
            List<HealthIssue> issues
    ) {
        for (Map.Entry<CurrencyCode, Money> entry :
                cashBalances.entrySet()) {

            CurrencyCode currency =
                    entry.getKey();

            Money balance =
                    entry.getValue();

            if (balance.amount().signum() < 0) {
                add(
                        issues,
                        "CASH_NEGATIVE",
                        currency.value(),
                        "error",
                        "El saldo de Investment Cash "
                                + currency.value()
                                + " es negativo."
                );
            }

            checkFx(
                    currency,
                    localCurrency,
                    now,
                    currency.value(),
                    issues
            );
        }
    }

    private void checkMarketData(
            UUID userId,
            AssetReference reference,
            CurrencyCode localCurrency,
            Instant now,
            List<HealthIssue> issues
    ) {
        switch (reference.kind()) {

            case SHARED -> {
                SharedAsset asset =
                        sharedAssets
                                .findById(
                                        reference.id()
                                )
                                .orElse(null);

                if (asset == null) {
                    return;
                }

                Optional<AssetPrice> price =
                        assetPrices.findLatestAt(
                                reference.id(),
                                now
                        );

                if (price.isEmpty()
                        || price.get().valuationPrice() == null) {

                    add(
                            issues,
                            "PRICE_MISSING",
                            reference.id().toString(),
                            "warning",
                            "El activo «"
                                    + asset.getName()
                                    + "» no tiene un precio disponible."
                    );

                } else {
                    AssetPrice latest =
                            price.get();

                    if (!asset.getCurrency().equals(
                            latest.currency()
                    )) {
                        add(
                                issues,
                                "PRICE_CURRENCY_MISMATCH",
                                reference.id().toString(),
                                "error",
                                "La moneda del precio de «"
                                        + asset.getName()
                                        + "» no coincide con la moneda del activo."
                        );

                    } else if (isStale(
                            latest.timestamp(),
                            now
                    )) {
                        add(
                                issues,
                                "PRICE_STALE",
                                reference.id().toString(),
                                "warning",
                                "El último precio de «"
                                        + asset.getName()
                                        + "» supera la antigüedad configurada."
                        );
                    }
                }

                checkFx(
                        asset.getCurrency(),
                        localCurrency,
                        now,
                        reference.id().toString(),
                        issues
                );
            }

            case USER -> {
                UserAsset asset =
                        userAssets
                                .findByIdAndUserId(
                                        reference.id(),
                                        userId
                                )
                                .orElse(null);

                if (asset == null) {
                    return;
                }

                Optional<UserAssetPrice> price =
                        userAssetPrices.findLatestAt(
                                reference.id(),
                                now
                        );

                if (price.isEmpty()) {
                    add(
                            issues,
                            "PRICE_MISSING",
                            reference.id().toString(),
                            "warning",
                            "El activo «"
                                    + asset.getName()
                                    + "» no tiene un precio disponible."
                    );
                } else {
                    UserAssetPrice latest =
                            price.get();

                    Money unitPrice =
                            latest.getUnitPrice();

                    if (!asset.getCurrency().equals(
                            unitPrice.currency()
                    )) {
                        add(
                                issues,
                                "PRICE_CURRENCY_MISMATCH",
                                reference.id().toString(),
                                "error",
                                "La moneda del precio de «"
                                        + asset.getName()
                                        + "» no coincide con la moneda del activo."
                        );

                    } else if (isStale(
                            latest.getTimestamp(),
                            now
                    )) {
                        add(
                                issues,
                                "PRICE_STALE",
                                reference.id().toString(),
                                "warning",
                                "El último precio de «"
                                        + asset.getName()
                                        + "» supera la antigüedad configurada."
                        );
                    }
                }

                checkFx(
                        asset.getCurrency(),
                        localCurrency,
                        now,
                        reference.id().toString(),
                        issues
                );
            }
        }
    }

    private void checkFx(
            CurrencyCode currency,
            CurrencyCode localCurrency,
            Instant now,
            String resourceId,
            List<HealthIssue> issues
    ) {
        if (currency == null
                || localCurrency == null
                || currency.equals(localCurrency)) {
            return;
        }

        Optional<FxRate> direct =
                fxRates.findLatestAt(
                        currency,
                        localCurrency,
                        now
                );

        Optional<FxRate> inverse =
                fxRates.findLatestAt(
                        localCurrency,
                        currency,
                        now
                );

        FxRate latest =
                latestRate(
                        direct.orElse(null),
                        inverse.orElse(null)
                );

        if (latest == null) {
            add(
                    issues,
                    "FX_MISSING",
                    resourceId,
                    "warning",
                    "Falta un tipo de cambio para convertir "
                            + currency.value()
                            + " a "
                            + localCurrency.value()
                            + "."
            );
            return;
        }

        if (isStale(
                latest.timestamp(),
                now
        )) {
            add(
                    issues,
                    "FX_STALE",
                    resourceId,
                    "warning",
                    "El tipo de cambio de "
                            + currency.value()
                            + " a "
                            + localCurrency.value()
                            + " supera la antigüedad configurada."
            );
        }
    }

    private FxRate latestRate(
            FxRate direct,
            FxRate inverse
    ) {
        if (direct == null) {
            return inverse;
        }

        if (inverse == null) {
            return direct;
        }

        if (inverse.timestamp().isAfter(
                direct.timestamp()
        )) {
            return inverse;
        }

        return direct;
    }

    private void checkTransfers(
            UUID userId,
            List<Activity> userActivities,
            List<HealthIssue> issues
    ) {
        for (Activity activity : userActivities) {

            ActivityType type =
                    activity.getType();

            if (type != ActivityType.DEPOSIT
                    && type != ActivityType.WITHDRAW) {
                continue;
            }

            UUID transactionId =
                    activity.getLinkedTransactionId();

            CashActivityDetails details =
                    (CashActivityDetails)
                            activity.getDetails();

            TransactionIntegrationPort.TransferDirection direction =
                    type == ActivityType.DEPOSIT
                            ? TransactionIntegrationPort.TransferDirection.TRANSFER_OUT
                            : TransactionIntegrationPort.TransferDirection.TRANSFER_IN;

            boolean synchronizedTransfer =
                    transactionId != null
                            && transactions.matchesInvestmentTransfer(
                                    userId,
                                    activity.getId(),
                                    transactionId,
                                    direction,
                                    details.amount()
                            );

            if (!synchronizedTransfer) {
                add(
                        issues,
                        "TRANSFER_OUT_OF_SYNC",
                        activity.getId().toString(),
                        "error",
                        "La transferencia vinculada a esta operación "
                                + "no coincide o no existe."
                );
            }
        }
    }

    private boolean isStale(
            Instant timestamp,
            Instant now
    ) {
        return timestamp
                .plus(maximumMarketDataAge)
                .isBefore(now);
    }

    private void add(
            List<HealthIssue> issues,
            String code,
            String resourceId,
            String severity,
            String message
    ) {
        issues.add(
                new HealthIssue(
                        code,
                        resourceId,
                        severity,
                        message
                )
        );
    }

    private String issueKey(
            HealthIssue issue
    ) {
        return issue.code()
                + ":"
                + String.valueOf(
                        issue.resourceId()
                );
    }

    private String safeMessage(
            RuntimeException exception
    ) {
        String message =
                exception.getMessage();

        if (message == null
                || message.isBlank()) {
            return "estado inconsistente";
        }

        return message.length() > 180
                ? message.substring(0, 180)
                : message;
    }

    private void validateUserId(
            UUID userId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }
    }
}