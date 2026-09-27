package com.puntomartinez.millete.investments.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases;
import com.puntomartinez.millete.investments.domain.ports.out.*;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvestmentPortfolioService implements InvestmentUseCases {
    private static final int MONEY_SCALE = 8;
    private final AssetRepository assets;
    private final ActivityRepository activities;
    private final HoldingRepository holdings;
    private final PortfolioRepository portfolio;
    private final MarketDataRepository marketData;
    private final UserCurrencyPort userCurrencies;
    private final DailyTransferPort dailyTransfers;
    private final InvestmentHealthService healthService;
    private final ObjectProvider<MarketDataProviderPort> providers;
    private final TimeProvider time;
    private final ObjectMapper mapper;

    public InvestmentPortfolioService(AssetRepository assets,
                                      ActivityRepository activities,
                                      HoldingRepository holdings,
                                      PortfolioRepository portfolio,
                                      MarketDataRepository marketData,
                                      UserCurrencyPort userCurrencies,
                                      DailyTransferPort dailyTransfers,
                                      InvestmentHealthService healthService,
                                      ObjectProvider<MarketDataProviderPort> providers,
                                      TimeProvider time,
                                      ObjectMapper mapper) {
        this.assets = assets;
        this.activities = activities;
        this.holdings = holdings;
        this.portfolio = portfolio;
        this.marketData = marketData;
        this.userCurrencies = userCurrencies;
        this.dailyTransfers = dailyTransfers;
        this.healthService = healthService;
        this.providers = providers;
        this.time = time;
        this.mapper = mapper;
    }

    @Override @Transactional
    public Asset createAsset(UUID userId, CreateAssetCommand command) {
        requireSector(command.sectorId());
        return assets.save(Asset.create(userId, command.name(), command.symbol(),
                command.type(), command.sectorId(), command.currency(), time.now()));
    }

    @Override @Transactional
    public Asset updateAsset(UUID userId, UUID assetId, CreateAssetCommand command) {
        Asset asset = requireAsset(userId, assetId);
        requireSector(command.sectorId());
        boolean hasHistory = !activities.findActivitiesByUserIdAndAssetId(userId, assetId).isEmpty()
                || holdings.findHoldingsIncludingSupersededByUserId(userId).stream()
                .anyMatch(holding -> holding.assetId().equals(assetId));
        if (hasHistory && !asset.getCurrency().equalsIgnoreCase(command.currency())) {
            throw new IllegalArgumentException("An Asset currency cannot change after investment history exists");
        }
        asset.update(command.name(), command.symbol(), command.type(), command.sectorId(), command.currency(), time.now());
        return assets.save(asset);
    }

    @Override @Transactional
    public void hideAsset(UUID userId, UUID assetId) {
        Asset asset = requireAsset(userId, assetId);
        activities.lockAsset(userId, assetId);
        asset.deactivate(time.now());
        assets.save(asset);
    }

    @Override @Transactional(readOnly = true)
    public List<Asset> listAssets(UUID userId, boolean includeInactive) {
        return assets.findAssetsByUserId(userId, includeInactive);
    }

    @Override @Transactional(readOnly = true)
    public List<AssetSector> listSectors() { return assets.findActiveSectors(); }

    @Override @Transactional
    public Activity recordActivity(UUID userId, RecordActivityCommand command, String idempotencyKey) {
        activities.lockUserPortfolio(userId);
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        String requestHash = fingerprint(command);
        Optional<ActivityRepository.ActivityRequest> previous =
                activities.findByUserIdAndIdempotencyKey(userId, normalizedKey);
        if (previous.isPresent()) {
            ActivityRepository.ActivityRequest request = previous.get();
            if (!request.requestHash().equals(requestHash)) {
                throw new InvalidInputException(
                        "La clave Idempotency-Key ya se usó con otra actividad."
                );
            }
            return request.activity();
        }

        validateActivityAsset(userId, command);
        Instant occurredAt = command.occurredAt() == null ? time.now() : command.occurredAt();
        String localCurrency = localCurrencyAt(userId, occurredAt);
        BigDecimal amount = command.amount();
        if ((command.type() == ActivityType.BUY || command.type() == ActivityType.SELL)
                && amount == null && command.quantity() != null && command.unitPrice() != null) {
            amount = command.quantity().multiply(command.unitPrice()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal localFx = null;
        String localFxSource = null;
        Instant localFxTimestamp = null;
        BigDecimal amountInLocal = null;
        if (command.type() != ActivityType.SPLIT && command.type() != ActivityType.EXCHANGE) {
            FxQuote quote = fxQuote(userId, command.currency(), localCurrency, occurredAt)
                    .orElseThrow(() -> new IllegalStateException("No historical FX rate is available for the selected date"));
            localFx = quote.rate();
            localFxSource = quote.source();
            localFxTimestamp = quote.timestamp();
            if (amount != null) amountInLocal = amount.multiply(localFx).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        Activity activity = Activity.create(userId, command.type(), occurredAt,
                command.assetId(), command.quantity(), command.unitPrice(), amount,
                command.currency(), command.secondaryAmount(), command.secondaryCurrency(),
                command.type() == ActivityType.EXCHANGE ? command.exchangeRate() : command.ratio(),
                localCurrency, localFx, localFxSource, localFxTimestamp,
                amountInLocal, command.comment(), time.now());
        // A sequence is allocated while the user portfolio lock is held.
        activity = rekey(activity, activities.nextOrderingKey(userId, occurredAt));
        Activity saved = activities.save(activity);

        if (saved.getType() == ActivityType.DEPOSIT || saved.getType() == ActivityType.WITHDRAW) {
            DailyTransferPort.TransferDirection direction = saved.getType() == ActivityType.DEPOSIT
                    ? DailyTransferPort.TransferDirection.TRANSFER_OUT
                    : DailyTransferPort.TransferDirection.TRANSFER_IN;
            UUID transactionId = dailyTransfers.createTransfer(userId, saved.getId(), direction,
                    saved.getAmountInLocal(), saved.getLocalCurrency(), saved.getOccurredAt(),
                    saved.getType().name() + " investment transfer");
            saved.attachTransaction(transactionId);
            saved = activities.save(saved);
        }
        activities.saveActivityRequest(userId, normalizedKey, requestHash, saved.getId(), time.now());
        rebuildUser(userId);
        return saved;
    }

    private String normalizeIdempotencyKey(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException("La cabecera Idempotency-Key es obligatoria.");
        }
        String key = value.trim();
        if (key.length() > 128) {
            throw new InvalidInputException("La cabecera Idempotency-Key no puede superar 128 caracteres.");
        }
        return key;
    }

    private String fingerprint(RecordActivityCommand command) {
        Objects.requireNonNull(command, "command");
        try {
            byte[] serialized = mapper.writeValueAsBytes(command);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(serialized));
        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se pudo calcular la huella de la actividad", exception);
        }
    }

    @Override @Transactional
    public Activity editActivity(UUID userId, UUID activityId, EditActivityCommand command) {
        activities.lockUserPortfolio(userId);
        Activity activity = requireActivity(userId, activityId);
        if (activity.getType() == ActivityType.DEPOSIT || activity.getType() == ActivityType.WITHDRAW) {
            throw new InvalidInputException(
                    "DEPOSIT y WITHDRAW son inmutables. Registra una Activity compensatoria para corregir el importe; solo se puede editar el comentario."
            );
        }
        Objects.requireNonNull(command.occurredAt(), "occurredAt is required");
        String before = json(activity);
        BigDecimal editedAmount = command.amount();
        if (activity.getType() != ActivityType.SPLIT && editedAmount == null
                && command.quantity() != null && command.unitPrice() != null) {
            editedAmount = command.quantity().multiply(command.unitPrice())
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        String localCurrency = null;
        BigDecimal localFx = null;
        String localFxSource = null;
        Instant localFxTimestamp = null;
        BigDecimal amountInLocal = null;
        if (activity.getType() != ActivityType.SPLIT) {
            if (editedAmount == null) throw new IllegalArgumentException("amount must be positive");
            localCurrency = localCurrencyAt(userId, command.occurredAt());
            FxQuote quote = fxQuote(userId, activity.getCurrency(), localCurrency, command.occurredAt())
                    .orElseThrow(() -> new IllegalStateException("No historical FX rate is available for the selected date"));
            localFx = quote.rate();
            localFxSource = quote.source();
            localFxTimestamp = quote.timestamp();
            amountInLocal = editedAmount.multiply(localFx).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        long orderingKey = activities.nextOrderingKey(userId, command.occurredAt());
        activity.editInvestmentDetails(command.occurredAt(), command.quantity(), command.unitPrice(),
                editedAmount, command.ratio(), command.comment(), orderingKey,
                localCurrency, localFx, amountInLocal,
                localFxSource, localFxTimestamp, time.now());
        Activity saved = activities.save(activity);
        activities.saveAudit(new ActivityAudit(UUID.randomUUID(), activityId, userId,
                before, json(saved), command.reason(), time.now()));
        rebuildUser(userId);
        return saved;
    }

    @Override @Transactional
    public Activity editTransferComment(UUID userId, UUID activityId, String comment) {
        activities.lockUserPortfolio(userId);
        Activity activity = requireActivity(userId, activityId);
        String before = json(activity);
        activity.editComment(comment, time.now());
        Activity saved = activities.save(activity);
        activities.saveAudit(new ActivityAudit(UUID.randomUUID(), activityId, userId,
                before, json(saved), "Comment updated", time.now()));
        return saved;
    }

    @Override @Transactional(readOnly = true)
    public List<Activity> listActivities(UUID userId, UUID assetId) {
        List<Activity> result = assetId == null ? activities.findActivitiesByUserId(userId)
                : activities.findActivitiesByUserIdAndAssetId(userId, assetId);
        return result.stream().sorted(activityOrder()).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<ActivityAudit> listActivityAudit(UUID userId, UUID activityId) {
        requireActivity(userId, activityId);
        return activities.findAuditByActivityId(activityId, userId);
    }

    @Override @Transactional
    public Holding createHolding(UUID userId, CreateHoldingCommand command) {
        activities.lockUserPortfolio(userId);
        Asset asset = requireAsset(userId, command.assetId());
        if (!asset.getCurrency().equalsIgnoreCase(command.currency())) throw new IllegalArgumentException("Holding currency must match Asset currency");
        Holding holding = new Holding(UUID.randomUUID(), userId, asset.getId(), command.snapshotAt(),
                command.quantity(), command.acquisitionCost(), command.currency(), true, false, time.now());
        Holding saved = holdings.save(holding);
        rebuildUser(userId);
        return saved;
    }

    @Override @Transactional
    public List<Activity> replaceHoldingWithHistory(UUID userId, UUID holdingId,
                                                    List<RecordActivityCommand> history) {
        activities.lockUserPortfolio(userId);
        Holding holding = holdings.findHoldingByIdAndUserId(holdingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding not found"));
        if (!holding.historyIncomplete()) {
            throw new IllegalArgumentException("Only an incomplete holding can be replaced with transaction history");
        }
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("At least one historical BUY or SELL is required");
        }

        Asset asset = requireAsset(userId, holding.assetId());
        List<Activity> created = new ArrayList<>();
        for (RecordActivityCommand command : history) {
            if (command.type() != ActivityType.BUY && command.type() != ActivityType.SELL) {
                throw new IllegalArgumentException("Holding history can contain BUY and SELL activities only");
            }
            if (!holding.assetId().equals(command.assetId())) {
                throw new IllegalArgumentException("All replacement activities must use the Holding Asset");
            }
            Instant occurredAt = Objects.requireNonNull(command.occurredAt(), "Historical activities require occurredAt");
            if (occurredAt.isAfter(holding.snapshotAt())) {
                throw new IllegalArgumentException("Replacement activities must be on or before the Holding snapshot");
            }
            if (!asset.getCurrency().equalsIgnoreCase(command.currency())) {
                throw new IllegalArgumentException("Historical trade currency must match the Asset currency");
            }
            BigDecimal amount = command.amount();
            if (amount == null && command.quantity() != null && command.unitPrice() != null) {
                amount = command.quantity().multiply(command.unitPrice()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            }
            if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Historical trade amount must be positive");
            String localCurrency = localCurrencyAt(userId, occurredAt);
            FxQuote quote = fxQuote(userId, command.currency(), localCurrency, occurredAt)
                    .orElseThrow(() -> new IllegalStateException("No historical FX rate is available for the selected date"));
            BigDecimal localAmount = amount.multiply(quote.rate()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            Activity activity = Activity.create(userId, command.type(), occurredAt, asset.getId(),
                    command.quantity(), command.unitPrice(), amount, command.currency(), null, null,
                    null, localCurrency, quote.rate(), quote.source(), quote.timestamp(),
                    localAmount, command.comment(), time.now());
            activity = rekey(activity, activities.nextOrderingKey(userId, occurredAt));
            created.add(activities.save(activity));
        }

        List<Activity> allActivities = activities.findActivitiesByUserId(userId);
        List<Holding> otherHoldings = holdings.findHoldingsByUserId(userId).stream()
                .filter(candidate -> !candidate.id().equals(holdingId)).toList();
        List<Activity> throughSnapshot = allActivities.stream()
                .filter(activity -> !activity.getOccurredAt().isAfter(holding.snapshotAt())).toList();
        List<Holding> holdingsThroughSnapshot = otherHoldings.stream()
                .filter(candidate -> !candidate.snapshotAt().isAfter(holding.snapshotAt())).toList();
        PortfolioReplay.Result historical = PortfolioReplay.replay(userId, throughSnapshot, holdingsThroughSnapshot);
        List<Lot> openLots = historical.lots().stream()
                .filter(lot -> lot.getAssetId().equals(holding.assetId()) && lot.getRemainingQuantity().signum() > 0)
                .toList();
        BigDecimal units = openLots.stream().map(Lot::getRemainingQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cost = openLots.stream().map(lot -> lot.getUnitCost().multiply(lot.getRemainingQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (units.compareTo(holding.quantity()) != 0 || cost.compareTo(holding.acquisitionCost()) != 0) {
            throw new IllegalArgumentException("Replacement history does not reconcile with the Holding quantity and acquisition cost");
        }

        holdings.markSuperseded(holdingId, userId);
        rebuildUser(userId);
        return List.copyOf(created);
    }

    @Override @Transactional(readOnly = true)
    public List<Holding> listHoldings(UUID userId, UUID assetId) {
        return assetId == null ? holdings.findHoldingsByUserId(userId) : holdings.findHoldingsByUserIdAndAssetId(userId, assetId);
    }

    @Override @Transactional
    public AssetPrice addPrice(UUID userId, AddPriceCommand command) {
        Asset asset = requireAsset(userId, command.assetId());
        if (!asset.getCurrency().equalsIgnoreCase(command.currency())) {
            throw new IllegalArgumentException("Asset price currency must match the Asset currency");
        }
        return marketData.savePrice(new AssetPrice(UUID.randomUUID(), userId, command.assetId(),
                command.timestamp(), command.open(), command.high(), command.low(), command.close(),
                command.adjustedClose(), command.volume(), command.currency(), command.source(), time.now()));
    }

    @Override @Transactional
    public FxRate addFxRate(UUID userId, AddFxRateCommand command) {
        if (userId == null) throw new IllegalArgumentException("A manually added FX rate must belong to a user");
        return marketData.saveFxRate(new FxRate(UUID.randomUUID(), userId, command.baseCurrency(),
                command.quoteCurrency(), command.timestamp(), command.rate(), command.source(), time.now()));
    }

    @Override @Transactional(readOnly = true)
    public List<AssetPrice> listPrices(UUID userId, UUID assetId, Instant from, Instant to) {
        requireAsset(userId, assetId);
        return marketData.pricesForAsset(userId, assetId, from, to);
    }

    @Override @Transactional(readOnly = true)
    public List<FxRate> listFxRates(UUID userId, Instant from, Instant to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new InvalidInputException("El rango de fechas FX no es válido.");
        }
        return marketData.fxRates(userId, from, to);
    }

    @Override @Transactional(readOnly = true)
    public Map<String, BigDecimal> cashBalances(UUID userId) {
        return PortfolioReplay.replay(userId, activities.findActivitiesByUserId(userId), holdings.findHoldingsByUserId(userId)).cashBalances();
    }

    @Override @Transactional(readOnly = true)
    public PortfolioView portfolio(UUID userId, Instant at) {
        Instant asOf = at == null ? time.now() : at;
        return portfolioAt(userId, asOf, localCurrencyAt(userId, asOf), false);
    }

    @Override @Transactional(readOnly = true)
    public List<ClosedLotView> closedLots(UUID userId, Instant from, Instant to) {
        Instant end = to == null ? time.now() : to;
        List<Activity> userActivities = activities.findActivitiesByUserId(userId).stream()
                .filter(activity -> !activity.getOccurredAt().isAfter(end))
                .sorted(activityOrder()).toList();
        List<Holding> userHoldings = holdings.findHoldingsByUserId(userId).stream()
                .filter(holding -> !holding.snapshotAt().isAfter(end)).toList();
        PortfolioReplay.Result replay = PortfolioReplay.replay(userId, userActivities, userHoldings);
        Map<UUID, Activity> byActivityId = userActivities.stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));
        Map<UUID, List<LotConsumption>> byLot = replay.consumptions().stream()
                .collect(Collectors.groupingBy(LotConsumption::lotId));
        Map<UUID, Asset> byAsset = assets.findAssetsByUserId(userId, true).stream()
                .collect(Collectors.toMap(Asset::getId, asset -> asset));
        List<ClosedLotView> result = new ArrayList<>();
        for (Lot lot : replay.lots()) {
            if (lot.getRemainingQuantity().signum() != 0) continue;
            List<LotConsumption> lotSales = byLot.getOrDefault(lot.getId(), List.of());
            if (lotSales.isEmpty()) continue;
            Instant closedAt = lotSales.stream().map(consumption -> byActivityId.get(consumption.sellActivityId()))
                    .filter(Objects::nonNull).map(Activity::getOccurredAt).max(Comparator.naturalOrder()).orElse(null);
            if (closedAt == null || (from != null && closedAt.isBefore(from))) continue;
            BigDecimal proceeds = BigDecimal.ZERO;
            for (LotConsumption consumption : lotSales) {
                Activity sale = byActivityId.get(consumption.sellActivityId());
                if (sale != null && sale.getQuantity() != null && sale.getQuantity().signum() > 0) {
                    proceeds = proceeds.add(sale.getAmount().multiply(consumption.quantity())
                            .divide(sale.getQuantity(), 16, RoundingMode.HALF_UP));
                }
            }
            BigDecimal costBasis = lotSales.stream().map(LotConsumption::costBasis)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Asset asset = byAsset.get(lot.getAssetId());
            if (asset == null) continue;
            result.add(new ClosedLotView(lot.getId(), asset.getId(), asset.getName(), asset.getSymbol(),
                    lot.getCurrency(), lot.getOriginalQuantity(), costBasis, proceeds,
                    proceeds.subtract(costBasis), lot.getAcquiredAt(), closedAt,
                    lot.isSynthetic(), lot.isSynthetic()));
        }
        result.sort(Comparator.comparing(ClosedLotView::closedAt).reversed());
        return List.copyOf(result);
    }

    private PortfolioView portfolioAt(UUID userId, Instant asOf, String local, boolean includeHiddenAssets) {
        List<Activity> activityList = activities.findActivitiesByUserId(userId).stream().filter(a -> !a.getOccurredAt().isAfter(asOf)).toList();
        List<Holding> holdingList = holdings.findHoldingsByUserId(userId).stream().filter(h -> !h.snapshotAt().isAfter(asOf)).toList();
        List<Holding> allHoldingHistory = holdings.findHoldingsIncludingSupersededByUserId(userId);
        PortfolioReplay.Result replay = PortfolioReplay.replay(userId, activityList, holdingList);
        Map<UUID, Asset> byId = assets.findAssetsByUserId(userId, true).stream().collect(Collectors.toMap(Asset::getId, a -> a));
        Map<UUID, List<Lot>> byAsset = replay.lots().stream().collect(Collectors.groupingBy(Lot::getAssetId));
        List<PositionView> positions = new ArrayList<>();
        List<CashValuationView> cashValuations = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean estimated = false;
        boolean complete = true;
        boolean historyIncomplete = allHoldingHistory.stream()
                .anyMatch(holding -> holding.historyIncomplete() && asOf.isBefore(holding.snapshotAt()));
        if (historyIncomplete) complete = false;
        for (Map.Entry<UUID, List<Lot>> entry : byAsset.entrySet()) {
            Asset asset = byId.get(entry.getKey());
            if (asset == null) {
                complete = false;
                continue;
            }
            if (!includeHiddenAssets && !isVisibleAt(asset, asOf)) continue;
            BigDecimal units = entry.getValue().stream().map(Lot::getRemainingQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (units.signum() == 0) continue;
            BigDecimal cost = entry.getValue().stream().filter(l -> l.getRemainingQuantity().signum() > 0)
                    .map(l -> l.getUnitCost().multiply(l.getRemainingQuantity())).reduce(BigDecimal.ZERO, BigDecimal::add);
            Optional<AssetPrice> quote = marketData.latestPriceAt(userId, asset.getId(), asOf);
            BigDecimal price = quote.map(AssetPrice::valuationPrice).orElse(null);
            Instant priceAt = quote.map(AssetPrice::timestamp).orElse(null);
            String priceSource = quote.map(AssetPrice::source).orElse(null);
            BigDecimal marketValue = null;
            BigDecimal gain = null;
            boolean priceEstimated = quote.isPresent() && !quote.get().timestamp().equals(asOf);
            Instant fxAt = null;
            String fxSource = null;
            BigDecimal fxRate = null;
            String valuationCurrency = local;
            Optional<FxQuote> conversion = fxQuote(userId, asset.getCurrency(), local, asOf);
            if (conversion.isPresent()) {
                fxRate = conversion.get().rate();
                fxAt = conversion.get().timestamp();
                fxSource = conversion.get().source();
            }
            boolean fxEstimated = conversion.isPresent() && !conversion.get().timestamp().equals(asOf);
            boolean posEstimated = priceEstimated || fxEstimated;
            String unavailableReason = null;
            ValuationStatus positionStatus;
            if (price == null || conversion.isEmpty()) {
                complete = false;
                if (price == null && conversion.isEmpty()) unavailableReason = "PRICE_AND_FX_UNAVAILABLE";
                else if (price == null) unavailableReason = "PRICE_UNAVAILABLE";
                else unavailableReason = "FX_UNAVAILABLE";
                positionStatus = ValuationStatus.NOT_CALCULABLE;
            } else {
                FxQuote fx = conversion.get();
                BigDecimal nativeValue = units.multiply(price);
                marketValue = nativeValue.multiply(fx.rate()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                BigDecimal localCost = cost.multiply(fx.rate()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                gain = marketValue.subtract(localCost);
                total = total.add(marketValue);
                positionStatus = posEstimated ? ValuationStatus.ESTIMATED : ValuationStatus.CALCULABLE;
            }
            estimated |= posEstimated;
            boolean incomplete = allHoldingHistory.stream().anyMatch(h -> h.assetId().equals(asset.getId())
                    && h.historyIncomplete()
                    && (h.superseded() ? asOf.isBefore(h.snapshotAt()) : !h.snapshotAt().isAfter(asOf)));
            positions.add(new PositionView(asset.getId(), asset.getName(), asset.getSymbol(), asset.getCurrency(),
                    units, cost, price, marketValue, gain, valuationCurrency, fxRate, priceAt,
                    priceSource, fxAt, fxSource, posEstimated, incomplete, positionStatus, unavailableReason));
        }
        for (Map.Entry<String, BigDecimal> balance : replay.cashBalances().entrySet()) {
            if (balance.getValue().signum() == 0) {
                cashValuations.add(new CashValuationView(balance.getKey(), balance.getValue(), null,
                        BigDecimal.ZERO, null, null, ValuationStatus.CALCULABLE));
                continue;
            }
            Optional<FxQuote> conversion = fxQuote(userId, balance.getKey(), local, asOf);
            if (conversion.isEmpty()) complete = false;
            else {
                FxQuote fx = conversion.get();
                BigDecimal amountInLocal = balance.getValue().multiply(fx.rate()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                total = total.add(amountInLocal);
                boolean cashEstimated = !fx.timestamp().equals(asOf);
                estimated |= cashEstimated;
                cashValuations.add(new CashValuationView(balance.getKey(), balance.getValue(), fx.rate(),
                        amountInLocal, fx.timestamp(), fx.source(),
                        cashEstimated ? ValuationStatus.ESTIMATED : ValuationStatus.CALCULABLE));
                continue;
            }
            cashValuations.add(new CashValuationView(balance.getKey(), balance.getValue(), null,
                    null, null, null, ValuationStatus.NOT_CALCULABLE));
        }
        positions.sort(Comparator.comparing(PositionView::assetName, String.CASE_INSENSITIVE_ORDER));
        cashValuations.sort(Comparator.comparing(CashValuationView::currency, String.CASE_INSENSITIVE_ORDER));
        return new PortfolioView(asOf, replay.cashBalances(), List.copyOf(positions), complete ? total : null,
                local, estimated, overallStatus(complete, estimated), historyIncomplete, List.copyOf(cashValuations));
    }

    @Override @Transactional(readOnly = true)
    public PerformanceAttribution performance(UUID userId, Instant from, Instant to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("The performance start must be before its end");
        }

        String reportingCurrency = localCurrencyAt(userId, to);
        // Hidden Assets remain part of the user's wealth even though normal portfolio queries omit them.
        PortfolioView opening = portfolioAt(userId, from, reportingCurrency, true);
        PortfolioView ending = portfolioAt(userId, to, reportingCurrency, true);
        if (opening.valuationStatus() == ValuationStatus.NOT_CALCULABLE
                || ending.valuationStatus() == ValuationStatus.NOT_CALCULABLE) {
            String reason = opening.valuationStatus() == ValuationStatus.NOT_CALCULABLE
                    ? "OPENING_VALUE_NOT_CALCULABLE" : "ENDING_VALUE_NOT_CALCULABLE";
            return PerformanceAttribution.notCalculable(from, to, reportingCurrency,
                    opening.totalInLocalCurrency(), ending.totalInLocalCurrency(), reason);
        }

        List<Activity> throughEnd = activities.findActivitiesByUserId(userId).stream()
                .filter(activity -> !activity.getOccurredAt().isAfter(to))
                .sorted(activityOrder()).toList();
        List<Holding> holdingsThroughEnd = holdings.findHoldingsByUserId(userId).stream()
                .filter(holding -> !holding.snapshotAt().isAfter(to)).toList();
        PortfolioReplay.Result replay = PortfolioReplay.replay(userId, throughEnd, holdingsThroughEnd);
        Map<UUID, BigDecimal> consumedCostBySale = replay.consumptions().stream()
                .collect(Collectors.groupingBy(LotConsumption::sellActivityId,
                        Collectors.mapping(LotConsumption::costBasis,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        BigDecimal contributions = BigDecimal.ZERO;
        BigDecimal withdrawals = BigDecimal.ZERO;
        BigDecimal realized = BigDecimal.ZERO;
        BigDecimal dividends = BigDecimal.ZERO;
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal openingCash = BigDecimal.ZERO;
        BigDecimal exchange = BigDecimal.ZERO;
        boolean estimated = opening.estimated() || ending.estimated();
        List<String> missing = new ArrayList<>();

        for (Activity activity : throughEnd) {
            if (!activity.getOccurredAt().isAfter(from)) continue;
            switch (activity.getType()) {
                case DEPOSIT -> {
                    ConvertedAmount amount = convertActivityAmount(activity, activity.getAmount(),
                            activity.getCurrency(), reportingCurrency);
                    if (amount == null) missing.add("DEPOSIT_FX:" + activity.getId());
                    else { contributions = contributions.add(amount.amount()); estimated |= amount.estimated(); }
                }
                case WITHDRAW -> {
                    ConvertedAmount amount = convertActivityAmount(activity, activity.getAmount(),
                            activity.getCurrency(), reportingCurrency);
                    if (amount == null) missing.add("WITHDRAW_FX:" + activity.getId());
                    else { withdrawals = withdrawals.add(amount.amount()); estimated |= amount.estimated(); }
                }
                case SELL -> {
                    BigDecimal consumedCost = consumedCostBySale.get(activity.getId());
                    if (consumedCost == null) {
                        missing.add("SELL_COST_BASIS:" + activity.getId());
                        continue;
                    }
                    ConvertedAmount gain = convertActivityAmount(activity,
                            activity.getAmount().subtract(consumedCost), activity.getCurrency(), reportingCurrency);
                    if (gain == null) missing.add("SELL_FX:" + activity.getId());
                    else { realized = realized.add(gain.amount()); estimated |= gain.estimated(); }
                }
                case DIVIDEND -> {
                    ConvertedAmount amount = convertActivityAmount(activity, activity.getAmount(),
                            activity.getCurrency(), reportingCurrency);
                    if (amount == null) missing.add("DIVIDEND_FX:" + activity.getId());
                    else { dividends = dividends.add(amount.amount()); estimated |= amount.estimated(); }
                }
                case INTEREST -> {
                    ConvertedAmount amount = convertActivityAmount(activity, activity.getAmount(),
                            activity.getCurrency(), reportingCurrency);
                    if (amount == null) missing.add("INTEREST_FX:" + activity.getId());
                    else { interest = interest.add(amount.amount()); estimated |= amount.estimated(); }
                }
                case OPENING_CASH -> {
                    ConvertedAmount amount = convertActivityAmount(activity, activity.getAmount(),
                            activity.getCurrency(), reportingCurrency);
                    if (amount == null) missing.add("OPENING_CASH_FX:" + activity.getId());
                    else { openingCash = openingCash.add(amount.amount()); estimated |= amount.estimated(); }
                }
                case EXCHANGE -> {
                    ConvertedAmount source = convertActivityAmount(activity, activity.getAmount(),
                            activity.getCurrency(), reportingCurrency);
                    ConvertedAmount target = convertActivityAmount(activity, activity.getSecondaryAmount(),
                            activity.getSecondaryCurrency(), reportingCurrency);
                    if (source == null || target == null) missing.add("EXCHANGE_FX:" + activity.getId());
                    else {
                        exchange = exchange.add(target.amount().subtract(source.amount()));
                        estimated |= source.estimated() || target.estimated();
                    }
                }
                case BUY, SPLIT -> { /* In-period cost-basis movements remain visible in the reconciliation difference. */ }
            }
        }

        BigDecimal openingUnrealized = unrealizedGain(opening);
        BigDecimal endingUnrealized = unrealizedGain(ending);
        if (openingUnrealized == null || endingUnrealized == null) missing.add("UNREALIZED_GAIN_NOT_CALCULABLE");
        FxEffect fxEffect = openingExposureFxEffect(userId, reportingCurrency, to, opening);
        if (fxEffect == null) missing.add("OPENING_EXPOSURE_FX_NOT_CALCULABLE");
        if (!missing.isEmpty()) {
            return PerformanceAttribution.notCalculable(from, to, reportingCurrency,
                    opening.totalInLocalCurrency(), ending.totalInLocalCurrency(),
                    String.join(",", missing.stream().distinct().toList()));
        }

        BigDecimal unrealizedChange = endingUnrealized.subtract(openingUnrealized);
        estimated |= fxEffect.estimated();
        return PerformanceAttribution.reconcile(from, to, reportingCurrency,
                opening.totalInLocalCurrency(), ending.totalInLocalCurrency(),
                contributions, withdrawals, realized, unrealizedChange, dividends, interest,
                openingCash, exchange, fxEffect.amount(), estimated);
    }

    private FxEffect openingExposureFxEffect(UUID userId, String reportingCurrency,
                                             Instant to, PortfolioView opening) {
        BigDecimal effect = BigDecimal.ZERO;
        boolean estimated = false;
        for (PositionView position : opening.positions()) {
            if (position.costBasis() == null || position.fxRateToLocal() == null) return null;
            if (position.costBasis().signum() == 0) continue;
            Optional<FxQuote> closingRate = fxQuote(userId, position.assetCurrency(), reportingCurrency, to);
            if (closingRate.isEmpty()) return null;
            effect = effect.add(position.costBasis()
                    .multiply(closingRate.get().rate().subtract(position.fxRateToLocal())));
            estimated |= !closingRate.get().timestamp().equals(to);
        }
        for (CashValuationView cash : opening.cashValuations()) {
            if (cash.balance().signum() == 0) continue;
            if (cash.fxRateToLocal() == null) return null;
            Optional<FxQuote> closingRate = fxQuote(userId, cash.currency(), reportingCurrency, to);
            if (closingRate.isEmpty()) return null;
            effect = effect.add(cash.balance()
                    .multiply(closingRate.get().rate().subtract(cash.fxRateToLocal())));
            estimated |= !closingRate.get().timestamp().equals(to);
        }
        return new FxEffect(effect.setScale(MONEY_SCALE, RoundingMode.HALF_UP), estimated);
    }

    private BigDecimal unrealizedGain(PortfolioView view) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PositionView position : view.positions()) {
            if (position.valuationStatus() == ValuationStatus.NOT_CALCULABLE || position.unrealizedGain() == null) return null;
            sum = sum.add(position.unrealizedGain());
        }
        return sum;
    }

    private ConvertedAmount convertActivityAmount(Activity activity, BigDecimal amount,
                                                   String fromCurrency, String toCurrency) {
        if (amount == null || fromCurrency == null) return null;
        if (fromCurrency.equalsIgnoreCase(toCurrency)) return new ConvertedAmount(amount, false);
        if (toCurrency.equalsIgnoreCase(activity.getLocalCurrency())
                && fromCurrency.equalsIgnoreCase(activity.getCurrency())
                && activity.getAmountInLocal() != null) {
            boolean estimated = activity.getFxRateTimestamp() != null
                    && !activity.getFxRateTimestamp().equals(activity.getOccurredAt());
            BigDecimal converted = activity.getAmount().signum() == 0 ? BigDecimal.ZERO
                    : amount.multiply(activity.getAmountInLocal()).divide(activity.getAmount(), 16, RoundingMode.HALF_UP);
            return new ConvertedAmount(converted, estimated);
        }
        Optional<FxQuote> quote = fxQuote(activity.getUserId(), fromCurrency, toCurrency, activity.getOccurredAt());
        if (quote.isEmpty()) return null;
        return new ConvertedAmount(amount.multiply(quote.get().rate()).setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                !quote.get().timestamp().equals(activity.getOccurredAt()));
    }

    private record ConvertedAmount(BigDecimal amount, boolean estimated) { }

    @Override
    public RefreshResult refreshFromProvider(UUID userId, Instant from, Instant to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("The refresh start must be before its end");
        }
        MarketDataProviderPort provider = providers.getIfAvailable();
        if (provider == null) throw new IllegalStateException("No external market data provider is configured");
        List<Asset> userAssets = assets.findAssetsByUserId(userId, false);
        List<AssetPrice> quotes = provider.fetchPrices(userAssets, from, to);
        Set<UUID> ownedAssetIds = userAssets.stream().map(Asset::getId).collect(Collectors.toSet());
        if (quotes.stream().anyMatch(q -> !ownedAssetIds.contains(q.assetId()))) {
            throw new IllegalArgumentException("Provider returned a price for an asset outside the user portfolio");
        }
        Set<String> marketCurrencies = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> localCurrencies = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        userAssets.stream().map(Asset::getCurrency).forEach(marketCurrencies::add);
        activities.findActivitiesByUserId(userId).forEach(activity -> {
            if (activity.getCurrency() != null) marketCurrencies.add(activity.getCurrency());
            if (activity.getSecondaryCurrency() != null) marketCurrencies.add(activity.getSecondaryCurrency());
            if (activity.getLocalCurrency() != null) localCurrencies.add(activity.getLocalCurrency());
        });
        String localCurrency = localCurrencyAt(userId, to);
        localCurrencies.add(localCurrency);
        List<String> currencyPairs = marketCurrencies.stream()
                .flatMap(currency -> localCurrencies.stream()
                        .filter(target -> !currency.equalsIgnoreCase(target))
                        .map(target -> currency.toUpperCase(Locale.ROOT) + "/" + target.toUpperCase(Locale.ROOT)))
                .distinct()
                .toList();
        List<FxRate> rates = provider.fetchFxRates(currencyPairs, from, to);
        marketData.saveMarketData(quotes, rates);
        return new RefreshResult(quotes.size(), rates.size(), provider.providerName());
    }

    @Override
    public HealthReport health(UUID userId) {
        return healthService.check(userId);
    }

    private void rebuildUser(UUID userId) {
        List<Activity> userActivities = activities.findActivitiesByUserId(userId);
        List<Holding> userHoldings = holdings.findHoldingsByUserId(userId);
        PortfolioReplay.Result result = PortfolioReplay.replay(userId, userActivities, userHoldings);
        Set<UUID> assetIds = new HashSet<>();
        userActivities.stream().map(Activity::getAssetId).filter(Objects::nonNull).forEach(assetIds::add);
        userHoldings.stream().map(Holding::assetId).forEach(assetIds::add);
        userAssetsIds(userId).forEach(assetIds::add);
        for (UUID assetId : assetIds) {
            List<Lot> lots = result.lots().stream().filter(l -> l.getAssetId().equals(assetId)).toList();
            Set<UUID> lotIds = lots.stream().map(Lot::getId).collect(Collectors.toSet());
            List<LotConsumption> consumptions = result.consumptions().stream().filter(c -> lotIds.contains(c.lotId())).toList();
            portfolio.replaceDerivedLots(userId, assetId, lots, consumptions);
        }
    }

    private Set<UUID> userAssetsIds(UUID userId) {
        return assets.findAssetsByUserId(userId, true).stream().map(Asset::getId).collect(Collectors.toSet());
    }

    private void validateActivityAsset(UUID userId, RecordActivityCommand command) {
        Objects.requireNonNull(command.type(), "Activity type is required");
        if (command.assetId() != null) {
            Asset asset = requireAsset(userId, command.assetId());
            if ((command.type() == ActivityType.BUY || command.type() == ActivityType.SELL)
                    && !asset.getCurrency().equalsIgnoreCase(command.currency())) {
                throw new IllegalArgumentException("BUY/SELL currency must match the Asset quote currency");
            }
        }
        if ((command.type() == ActivityType.BUY || command.type() == ActivityType.SELL || command.type() == ActivityType.SPLIT)
                && command.assetId() == null) throw new IllegalArgumentException("This activity requires an Asset");
    }

    private Asset requireAsset(UUID userId, UUID assetId) {
        return assets.findAssetByIdAndUserId(assetId, userId).filter(Asset::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    private Activity requireActivity(UUID userId, UUID activityId) {
        return activities.findActivityByIdAndUserId(activityId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
    }

    private void requireSector(UUID sectorId) {
        if (sectorId != null && !assets.sectorExists(sectorId)) throw new ResourceNotFoundException("Asset sector not found");
    }

    private String localCurrencyAt(UUID userId, Instant at) {
        return userCurrencies.currencyAt(userId, at).map(UserLocalCurrencyPeriod::currency)
                .or(() -> userCurrencies.currentCurrency(userId)).orElseThrow(() -> new IllegalStateException("Set a local currency in profile before using investments"));
    }

    private boolean isVisibleAt(Asset asset, Instant at) {
        return !at.isBefore(asset.getCreatedAt()) && (asset.isActive() || at.isBefore(asset.getModifiedAt()));
    }

    private ValuationStatus overallStatus(boolean complete, boolean estimated) {
        if (!complete) return ValuationStatus.NOT_CALCULABLE;
        return estimated ? ValuationStatus.ESTIMATED : ValuationStatus.CALCULABLE;
    }

    private Optional<FxQuote> fxQuote(UUID userId, String from, String to, Instant at) {
        if (from.equalsIgnoreCase(to)) return Optional.of(new FxQuote(BigDecimal.ONE, at, "IDENTITY"));
        Optional<FxRate> direct = marketData.latestFxAt(userId, from, to, at);
        Optional<FxRate> inverse = marketData.latestFxAt(userId, to, from, at);
        if (direct.isPresent() && (inverse.isEmpty()
                || !inverse.get().timestamp().isAfter(direct.get().timestamp()))) {
            FxRate rate = direct.get();
            return Optional.of(new FxQuote(rate.rate(), rate.timestamp(), rate.source()));
        }
        return inverse.map(rate -> new FxQuote(
                BigDecimal.ONE.divide(rate.rate(), 16, RoundingMode.HALF_UP),
                rate.timestamp(), "INVERSE:" + rate.source()));
    }

    private Activity rekey(Activity activity, long orderingKey) {
        return Activity.reconstitute(activity.getId(), activity.getUserId(), activity.getType(),
                activity.getOccurredAt(), activity.getCreatedAt(), activity.getModifiedAt(), orderingKey,
                activity.getAssetId(), activity.getQuantity(), activity.getUnitPrice(), activity.getAmount(),
                activity.getCurrency(), activity.getSecondaryAmount(), activity.getSecondaryCurrency(),
                activity.getRatio(),
                activity.getLocalCurrency(), activity.getFxRateToLocal(),
                activity.getFxRateSource(), activity.getFxRateTimestamp(), activity.getAmountInLocal(),
                activity.getComment(), activity.getLinkedTransactionId());
    }

    private String json(Activity activity) {
        try { return mapper.writeValueAsString(activity); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Could not serialize Activity audit snapshot", e); }
    }

    private Comparator<Activity> activityOrder() {
        return Comparator.comparing(Activity::getOccurredAt).thenComparingLong(Activity::getOrderingKey).thenComparing(a -> a.getId().toString());
    }

    private record FxQuote(BigDecimal rate, Instant timestamp, String source) { }
    private record FxEffect(BigDecimal amount, boolean estimated) { }
}
