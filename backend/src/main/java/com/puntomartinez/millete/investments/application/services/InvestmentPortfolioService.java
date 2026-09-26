package com.puntomartinez.millete.investments.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases;
import com.puntomartinez.millete.investments.domain.ports.out.*;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public Activity recordActivity(UUID userId, RecordActivityCommand command) {
        activities.lockUserPortfolio(userId);
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
            FxQuote quote = fxQuote(command.currency(), localCurrency, occurredAt)
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
        rebuildUser(userId);
        return saved;
    }

    @Override @Transactional
    public Activity editActivity(UUID userId, UUID activityId, EditActivityCommand command) {
        activities.lockUserPortfolio(userId);
        Activity activity = requireActivity(userId, activityId);
        if (activity.getType() == ActivityType.DEPOSIT || activity.getType() == ActivityType.WITHDRAW) {
            throw new IllegalArgumentException("DEPOSIT and WITHDRAW fields are immutable; only comment can change");
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
            FxQuote quote = fxQuote(activity.getCurrency(), localCurrency, command.occurredAt())
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
            FxQuote quote = fxQuote(command.currency(), localCurrency, occurredAt)
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
    public FxRate addFxRate(AddFxRateCommand command) {
        return marketData.saveFxRate(new FxRate(UUID.randomUUID(), command.baseCurrency(),
                command.quoteCurrency(), command.timestamp(), command.rate(), command.source(), time.now()));
    }

    @Override @Transactional(readOnly = true)
    public List<AssetPrice> listPrices(UUID userId, UUID assetId, Instant from, Instant to) {
        requireAsset(userId, assetId);
        return marketData.pricesForAsset(userId, assetId, from, to);
    }

    @Override @Transactional(readOnly = true)
    public Map<String, BigDecimal> cashBalances(UUID userId) {
        return PortfolioReplay.replay(userId, activities.findActivitiesByUserId(userId), holdings.findHoldingsByUserId(userId)).cashBalances();
    }

    @Override @Transactional(readOnly = true)
    public PortfolioView portfolio(UUID userId, Instant at) {
        Instant asOf = at == null ? time.now() : at;
        List<Activity> activityList = activities.findActivitiesByUserId(userId).stream().filter(a -> !a.getOccurredAt().isAfter(asOf)).toList();
        List<Holding> holdingList = holdings.findHoldingsByUserId(userId).stream().filter(h -> !h.snapshotAt().isAfter(asOf)).toList();
        List<Holding> allHoldingHistory = holdings.findHoldingsIncludingSupersededByUserId(userId);
        PortfolioReplay.Result replay = PortfolioReplay.replay(userId, activityList, holdingList);
        String local = localCurrencyAt(userId, asOf);
        Map<UUID, Asset> byId = assets.findAssetsByUserId(userId, true).stream().collect(Collectors.toMap(Asset::getId, a -> a));
        Map<UUID, List<Lot>> byAsset = replay.lots().stream().collect(Collectors.groupingBy(Lot::getAssetId));
        List<PositionView> positions = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean estimated = false;
        boolean complete = true;
        for (Map.Entry<UUID, List<Lot>> entry : byAsset.entrySet()) {
            Asset asset = byId.get(entry.getKey());
            if (asset == null || !asset.isActive()) continue;
            BigDecimal units = entry.getValue().stream().map(Lot::getRemainingQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (units.signum() == 0) continue;
            BigDecimal cost = entry.getValue().stream().filter(l -> l.getRemainingQuantity().signum() > 0)
                    .map(l -> l.getUnitCost().multiply(l.getRemainingQuantity())).reduce(BigDecimal.ZERO, BigDecimal::add);
            Optional<AssetPrice> quote = marketData.latestPriceAt(userId, asset.getId(), asOf);
            BigDecimal price = quote.map(AssetPrice::close).orElse(null);
            Instant priceAt = quote.map(AssetPrice::timestamp).orElse(null);
            String priceSource = quote.map(AssetPrice::source).orElse(null);
            BigDecimal marketValue = null;
            BigDecimal gain = null;
            boolean posEstimated = quote.isPresent() && !quote.get().timestamp().equals(asOf);
            Instant fxAt = null;
            String fxSource = null;
            String valuationCurrency = local;
            if (price == null) complete = false;
            else {
                BigDecimal nativeValue = units.multiply(price);
                Optional<FxQuote> conversion = fxQuote(asset.getCurrency(), local, asOf);
                if (conversion.isEmpty()) complete = false;
                else {
                    FxQuote fx = conversion.get();
                    fxAt = fx.timestamp();
                    fxSource = fx.source();
                    marketValue = nativeValue.multiply(fx.rate()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                    BigDecimal localCost = cost.multiply(fx.rate()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                    gain = marketValue.subtract(localCost);
                    total = total.add(marketValue);
                    posEstimated |= !fx.timestamp().equals(asOf);
                }
            }
            estimated |= posEstimated;
            boolean incomplete = allHoldingHistory.stream().anyMatch(h -> h.assetId().equals(asset.getId())
                    && h.historyIncomplete()
                    && (h.superseded() ? asOf.isBefore(h.snapshotAt()) : !h.snapshotAt().isAfter(asOf)));
            positions.add(new PositionView(asset.getId(), asset.getName(), asset.getSymbol(), asset.getCurrency(),
                    units, cost, price, marketValue, gain, valuationCurrency, priceAt,
                    priceSource, fxAt, fxSource, posEstimated, incomplete));
        }
        for (Map.Entry<String, BigDecimal> balance : replay.cashBalances().entrySet()) {
            Optional<FxQuote> conversion = fxQuote(balance.getKey(), local, asOf);
            if (conversion.isEmpty()) complete = false;
            else {
                FxQuote fx = conversion.get();
                total = total.add(balance.getValue().multiply(fx.rate()).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
                if (!fx.timestamp().equals(asOf)) estimated = true;
            }
        }
        positions.sort(Comparator.comparing(PositionView::assetName, String.CASE_INSENSITIVE_ORDER));
        return new PortfolioView(asOf, replay.cashBalances(), List.copyOf(positions), complete ? total : null, local, estimated);
    }

    @Override @Transactional
    public RefreshResult refreshFromProvider(UUID userId, Instant from, Instant to) {
        MarketDataProviderPort provider = providers.getIfAvailable();
        if (provider == null) throw new IllegalStateException("No external market data provider is configured");
        List<Asset> userAssets = assets.findAssetsByUserId(userId, false);
        List<AssetPrice> quotes = provider.fetchPrices(userAssets, from, to);
        quotes.forEach(q -> {
            if (!userAssets.stream().anyMatch(a -> a.getId().equals(q.assetId()))) throw new IllegalArgumentException("Provider returned a price for an asset outside the user portfolio");
            marketData.savePrice(q);
        });
        List<FxRate> rates = provider.fetchFxRates(List.of(), from, to);
        rates.forEach(marketData::saveFxRate);
        return new RefreshResult(quotes.size(), rates.size(), provider.getClass().getSimpleName());
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

    private Optional<FxQuote> fxQuote(String from, String to, Instant at) {
        if (from.equalsIgnoreCase(to)) return Optional.of(new FxQuote(BigDecimal.ONE, at, "IDENTITY"));
        Optional<FxRate> direct = marketData.latestFxAt(from, to, at);
        if (direct.isPresent()) {
            FxRate rate = direct.get();
            return Optional.of(new FxQuote(rate.rate(), rate.timestamp(), rate.source()));
        }
        return marketData.latestFxAt(to, from, at).map(rate -> new FxQuote(
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
}
