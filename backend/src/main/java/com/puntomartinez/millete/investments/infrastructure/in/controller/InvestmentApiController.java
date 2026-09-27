package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentApiDTOs.*;
import com.puntomartinez.millete.investments.infrastructure.out.marketdata.MarketDataProviderException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.ErrorResponseDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Versioned resource API for user owned investment records. */
@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentApiController {
    private static final int MAX_PAGE_SIZE = 200;
    private final InvestmentUseCases useCases;

    public InvestmentApiController(InvestmentUseCases useCases) { this.useCases = useCases; }

    @PostMapping("/assets")
    public ResponseEntity<AssetResponseDTO> createAsset(@Valid @RequestBody CreateAssetRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(asset(useCases.createAsset(user(authentication), assetCommand(request))));
    }

    @PutMapping("/assets/{id}")
    public AssetResponseDTO updateAsset(@PathVariable UUID id, @Valid @RequestBody CreateAssetRequestDTO request,
            Authentication authentication) {
        return asset(useCases.updateAsset(user(authentication), id, assetCommand(request)));
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Void> hideAsset(@PathVariable UUID id, Authentication authentication) {
        useCases.hideAsset(user(authentication), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assets")
    public PaginatedResponseDTO<AssetResponseDTO> assets(
            @RequestParam(defaultValue="false") boolean includeInactive,
            @RequestParam(required=false) AssetType type,
            @RequestParam(required=false) UUID sectorId,
            @RequestParam(required=false) String currency,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="50") int size,
            Authentication authentication) {
        validatePage(page, size);
        List<AssetResponseDTO> filtered = useCases.listAssets(user(authentication), includeInactive).stream()
                .filter(a -> type == null || a.getType() == type)
                .filter(a -> sectorId == null || sectorId.equals(a.getSectorId()))
                .filter(a -> currency == null || a.getCurrency().equalsIgnoreCase(currency))
                .map(this::asset).toList();
        return page(filtered, page, size);
    }

    @GetMapping("/sectors")
    public List<SectorResponseDTO> sectors() {
        return useCases.listSectors().stream()
                .map(s -> new SectorResponseDTO(s.id(), s.code(), s.displayName())).toList();
    }

    @PostMapping("/activities/buy")
    public ResponseEntity<ActivityResponseDTO> buy(@Valid @RequestBody BuyActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.BUY, r.occurredAt(), r.assetId(), r.quantity(),
                r.unitPrice(), r.amount(), r.currency(), null, null, null, null, r.comment());
    }

    @PostMapping("/activities/sell")
    public ResponseEntity<ActivityResponseDTO> sell(@Valid @RequestBody SellActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.SELL, r.occurredAt(), r.assetId(), r.quantity(),
                r.unitPrice(), r.amount(), r.currency(), null, null, null, null, r.comment());
    }

    @PostMapping("/activities/dividends")
    public ResponseEntity<ActivityResponseDTO> dividend(@Valid @RequestBody DividendActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.DIVIDEND, r.occurredAt(), r.assetId(), null,
                null, r.amount(), r.currency(), null, null, null, null, r.comment());
    }

    @PostMapping("/activities/interest")
    public ResponseEntity<ActivityResponseDTO> interest(@Valid @RequestBody InterestActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.INTEREST, r.occurredAt(), r.assetId(), null,
                null, r.amount(), r.currency(), null, null, null, null, r.comment());
    }

    @PostMapping("/activities/deposits")
    public ResponseEntity<ActivityResponseDTO> deposit(@Valid @RequestBody DepositActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.DEPOSIT, r.occurredAt(), null, null,
                null, r.amount(), r.currency(), null, null, null, null, r.comment());
    }

    @PostMapping("/activities/withdrawals")
    public ResponseEntity<ActivityResponseDTO> withdraw(@Valid @RequestBody WithdrawActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.WITHDRAW, r.occurredAt(), null, null,
                null, r.amount(), r.currency(), null, null, null, null, r.comment());
    }

    @PostMapping("/activities/opening-cash")
    public ResponseEntity<ActivityResponseDTO> openingCash(@Valid @RequestBody OpeningCashActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.OPENING_CASH, r.occurredAt(), null, null,
                null, r.amount(), r.currency(), null, null, null, null, r.comment());
    }

    @PostMapping("/activities/splits")
    public ResponseEntity<ActivityResponseDTO> split(@Valid @RequestBody SplitActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.SPLIT, r.occurredAt(), r.assetId(), null,
                null, null, null, null, null, r.ratio(), null, r.comment());
    }

    @PostMapping("/activities/exchanges")
    public ResponseEntity<ActivityResponseDTO> exchange(@Valid @RequestBody ExchangeActivityRequestDTO r,
            @RequestHeader("Idempotency-Key") String key, Authentication a) {
        return createActivity(a, key, ActivityType.EXCHANGE, r.occurredAt(), null, null,
                null, r.amount(), r.currency(), r.secondaryAmount(), r.secondaryCurrency(), null,
                r.exchangeRate(), r.comment());
    }

    @GetMapping("/activities")
    public PaginatedResponseDTO<ActivityResponseDTO> activities(
            @RequestParam(required=false) UUID assetId,
            @RequestParam(required=false) ActivityType type,
            @RequestParam(required=false) AssetType assetType,
            @RequestParam(required=false) UUID sectorId,
            @RequestParam(required=false) String currency,
            @RequestParam(required=false) Instant from,
            @RequestParam(required=false) Instant to,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="50") int size,
            Authentication authentication) {
        validatePage(page, size);
        if (from != null && to != null && from.isAfter(to)) throw new InvalidInputException("El rango de fechas no es válido.");
        UUID userId = user(authentication);
        Map<UUID, Asset> assets = useCases.listAssets(userId, true).stream()
                .collect(Collectors.toMap(Asset::getId, Function.identity()));
        List<ActivityResponseDTO> filtered = useCases.listActivities(userId, assetId).stream()
                .filter(a -> type == null || a.getType() == type)
                .filter(a -> from == null || !a.getOccurredAt().isBefore(from))
                .filter(a -> to == null || !a.getOccurredAt().isAfter(to))
                .filter(a -> currency == null || (a.getCurrency() != null && a.getCurrency().equalsIgnoreCase(currency))
                        || (a.getSecondaryCurrency() != null && a.getSecondaryCurrency().equalsIgnoreCase(currency)))
                .filter(a -> (assetType == null && sectorId == null) || (a.getAssetId() != null && assets.containsKey(a.getAssetId())))
                .filter(a -> assetType == null || (a.getAssetId() != null && assets.get(a.getAssetId()).getType() == assetType))
                .filter(a -> sectorId == null || (a.getAssetId() != null && sectorId.equals(assets.get(a.getAssetId()).getSectorId())))
                .map(this::activity).toList();
        return page(filtered, page, size);
    }

    @PatchMapping("/activities/{id}")
    public ActivityResponseDTO editActivity(@PathVariable UUID id,
            @Valid @RequestBody EditActivityRequestDTO r, Authentication a) {
        return activity(useCases.editActivity(user(a), id, new InvestmentUseCases.EditActivityCommand(
                r.occurredAt(), r.quantity(), r.unitPrice(), r.amount(), r.ratio(), r.comment(), r.reason())));
    }

    @PatchMapping("/activities/{id}/comment")
    public ActivityResponseDTO editTransferComment(@PathVariable UUID id,
            @Valid @RequestBody CommentRequestDTO r, Authentication a) {
        return activity(useCases.editTransferComment(user(a), id, r.comment()));
    }

    @GetMapping("/activities/{id}/audit")
    public List<ActivityAuditResponseDTO> activityAudit(@PathVariable UUID id, Authentication a) {
        return useCases.listActivityAudit(user(a), id).stream().map(row -> new ActivityAuditResponseDTO(
                row.id(), row.activityId(), row.beforeJson(), row.afterJson(), row.reason(), row.changedAt())).toList();
    }

    @PostMapping("/holdings")
    public ResponseEntity<HoldingResponseDTO> createHolding(@Valid @RequestBody CreateHoldingRequestDTO r,
            Authentication a) {
        Holding holding = useCases.createHolding(user(a), new InvestmentUseCases.CreateHoldingCommand(
                r.assetId(), r.snapshotAt(), r.quantity(), r.acquisitionCost(), r.currency()));
        return ResponseEntity.status(HttpStatus.CREATED).body(holding(holding));
    }

    @PostMapping("/holdings/{id}/history")
    public List<ActivityResponseDTO> replaceHoldingWithHistory(@PathVariable UUID id,
            @Valid @RequestBody ReplaceHoldingHistoryRequestDTO r, Authentication a) {
        return useCases.replaceHoldingWithHistory(user(a), id, r.activities().stream()
                .map(this::activityCommand).toList()).stream().map(this::activity).toList();
    }

    @GetMapping("/holdings")
    public PaginatedResponseDTO<HoldingResponseDTO> holdings(@RequestParam(required=false) UUID assetId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size,
            Authentication a) {
        validatePage(page, size);
        return page(useCases.listHoldings(user(a), assetId).stream().map(this::holding).toList(), page, size);
    }

    @GetMapping("/cash")
    public List<CashBalanceResponseDTO> cash(Authentication a) {
        return useCases.cashBalances(user(a)).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new CashBalanceResponseDTO(entry.getKey(), entry.getValue())).toList();
    }

    @GetMapping("/portfolio")
    public PortfolioResponseDTO portfolio(@RequestParam(required=false) Instant at, Authentication a) {
        return portfolio(useCases.portfolio(user(a), at));
    }

    @GetMapping("/positions/open")
    public PaginatedResponseDTO<PositionResponseDTO> openPositions(
            @RequestParam(required=false) UUID assetId, @RequestParam(required=false) AssetType assetType,
            @RequestParam(required=false) UUID sectorId, @RequestParam(required=false) String currency,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size,
            Authentication a) {
        validatePage(page, size);
        InvestmentUseCases.PortfolioView view = useCases.portfolio(user(a), null);
        Map<UUID, Asset> assets = useCases.listAssets(user(a), true).stream().collect(Collectors.toMap(Asset::getId, Function.identity()));
        List<PositionResponseDTO> filtered = view.positions().stream()
                .filter(p -> assetId == null || assetId.equals(p.assetId()))
                .filter(p -> currency == null || p.assetCurrency().equalsIgnoreCase(currency))
                .filter(p -> assetType == null || (assets.containsKey(p.assetId()) && assets.get(p.assetId()).getType() == assetType))
                .filter(p -> sectorId == null || (assets.containsKey(p.assetId()) && sectorId.equals(assets.get(p.assetId()).getSectorId())))
                .map(this::position).toList();
        return page(filtered, page, size);
    }

    /** Each page item is one fully consumed FIFO lot, not an aggregate by asset. */
    @GetMapping("/lots/closed")
    public PaginatedResponseDTO<ClosedLotResponseDTO> closedLots(
            @RequestParam(required=false) UUID assetId, @RequestParam(required=false) AssetType assetType,
            @RequestParam(required=false) UUID sectorId, @RequestParam(required=false) String currency,
            @RequestParam(required=false) Instant from, @RequestParam(required=false) Instant to,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size,
            Authentication a) {
        validatePage(page, size);
        if (from != null && to != null && from.isAfter(to)) throw new InvalidInputException("El rango de fechas no es válido.");
        UUID userId = user(a);
        Map<UUID, Asset> assets = useCases.listAssets(userId, true).stream().collect(Collectors.toMap(Asset::getId, Function.identity()));
        List<ClosedLotResponseDTO> filtered = useCases.closedLots(userId, from, to).stream()
                .filter(p -> assetId == null || assetId.equals(p.assetId()))
                .filter(p -> currency == null || p.currency().equalsIgnoreCase(currency))
                .filter(p -> assetType == null || (assets.containsKey(p.assetId()) && assets.get(p.assetId()).getType() == assetType))
                .filter(p -> sectorId == null || (assets.containsKey(p.assetId()) && sectorId.equals(assets.get(p.assetId()).getSectorId())))
                .map(p -> new ClosedLotResponseDTO(p.lotId(), p.assetId(), p.assetName(), p.symbol(), p.currency(),
                        p.quantity(), p.costBasis(), p.proceeds(), p.realizedGain(), p.openedAt(), p.closedAt(),
                        p.estimated(), p.historyIncomplete())).toList();
        return page(filtered, page, size);
    }

    @PostMapping("/prices")
    public ResponseEntity<AssetPriceResponseDTO> addPrice(@Valid @RequestBody AddAssetPriceRequestDTO r,
            Authentication a) {
        AssetPrice price = useCases.addPrice(user(a), new InvestmentUseCases.AddPriceCommand(r.assetId(), r.timestamp(),
                r.open(), r.high(), r.low(), r.close(), r.adjustedClose(), r.volume(), r.currency(), r.source()));
        return ResponseEntity.status(HttpStatus.CREATED).body(price(price));
    }

    @GetMapping("/assets/{assetId}/prices")
    public PaginatedResponseDTO<AssetPriceResponseDTO> prices(@PathVariable UUID assetId,
            @RequestParam Instant from, @RequestParam Instant to,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size,
            Authentication a) {
        validatePage(page, size);
        if (from.isAfter(to)) throw new InvalidInputException("El rango de fechas no es válido.");
        return page(useCases.listPrices(user(a), assetId, from, to).stream().map(this::price).toList(), page, size);
    }

    @PostMapping("/fx-rates")
    public ResponseEntity<FxRateResponseDTO> addFxRate(@Valid @RequestBody AddFxRateRequestDTO r,
            Authentication a) {
        FxRate rate = useCases.addFxRate(user(a), new InvestmentUseCases.AddFxRateCommand(r.baseCurrency(), r.quoteCurrency(),
                r.timestamp(), r.rate(), r.source()));
        return ResponseEntity.status(HttpStatus.CREATED).body(fxRate(rate));
    }

    @GetMapping("/fx-rates")
    public PaginatedResponseDTO<FxRateResponseDTO> fxRates(@RequestParam Instant from, @RequestParam Instant to,
            @RequestParam(required=false) String baseCurrency, @RequestParam(required=false) String quoteCurrency,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size,
            Authentication a) {
        validatePage(page, size);
        return page(useCases.listFxRates(user(a), from, to).stream()
                .filter(rate -> baseCurrency == null || rate.baseCurrency().equalsIgnoreCase(baseCurrency))
                .filter(rate -> quoteCurrency == null || rate.quoteCurrency().equalsIgnoreCase(quoteCurrency))
                .map(this::fxRate).toList(), page, size);
    }

    @GetMapping("/metrics/performance")
    public PerformanceResponseDTO performance(@RequestParam Instant from, @RequestParam Instant to,
            Authentication a) {
        PerformanceAttribution result = useCases.performance(user(a), from, to);
        return new PerformanceResponseDTO(result.from(), result.to(), result.currency(), result.openingValue(),
                result.endingValue(), result.portfolioChange(), result.contributions(), result.withdrawals(),
                result.netExternalFlows(), result.realizedGains(), result.unrealizedGainChange(), result.dividends(),
                result.interest(), result.fxEffect(), result.openingCashAdjustments(), result.exchangeAdjustments(),
                result.reconciliationDifference(), result.estimated(),
                result.status() == PerformanceAttribution.Status.NOT_CALCULABLE,
                result.status().name(), result.unavailableReason());
    }

    @PostMapping("/market-data/refresh")
    public RefreshResponseDTO refresh(@RequestParam Instant from, @RequestParam Instant to, Authentication a) {
        InvestmentUseCases.RefreshResult result = useCases.refreshFromProvider(user(a), from, to);
        return new RefreshResponseDTO(result.pricesStored(), result.fxRatesStored(), result.provider());
    }

    @GetMapping("/health")
    public HealthCenterResponseDTO health(Authentication a) {
        InvestmentUseCases.HealthReport report = useCases.health(user(a));
        return new HealthCenterResponseDTO(report.checkedAt(), report.issues().stream()
                .map(issue -> new HealthIssueResponseDTO(issue.key(), issue.code(), issue.resourceId(),
                        issue.severity(), issue.message())).toList());
    }

    @ExceptionHandler(MarketDataProviderException.class)
    public ResponseEntity<ErrorResponseDTO> marketDataProviderFailure(MarketDataProviderException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                exception.getMessage(), "/api/v1/investments/market-data/refresh"));
    }

    private ResponseEntity<ActivityResponseDTO> createActivity(Authentication authentication, String key,
            ActivityType type, Instant occurredAt, UUID assetId, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal amount, String currency, BigDecimal secondaryAmount, String secondaryCurrency,
            BigDecimal ratio, BigDecimal exchangeRate, String comment) {
        InvestmentUseCases.RecordActivityCommand command = new InvestmentUseCases.RecordActivityCommand(type,
                occurredAt, assetId, quantity, unitPrice, amount, currency, secondaryAmount, secondaryCurrency,
                ratio, exchangeRate, comment);
        Activity activity = useCases.recordActivity(user(authentication), command, key);
        return ResponseEntity.status(HttpStatus.CREATED).body(activity(activity));
    }

    private InvestmentUseCases.RecordActivityCommand activityCommand(RecordActivityRequestDTO r) {
        return new InvestmentUseCases.RecordActivityCommand(r.type(), r.occurredAt(), r.assetId(), r.quantity(),
                r.unitPrice(), r.amount(), r.currency(), r.secondaryAmount(), r.secondaryCurrency(), r.ratio(),
                r.exchangeRate(), r.comment());
    }

    private InvestmentUseCases.CreateAssetCommand assetCommand(CreateAssetRequestDTO r) {
        return new InvestmentUseCases.CreateAssetCommand(r.name(), r.symbol(), r.type(), r.sectorId(), r.currency());
    }
    private UUID user(Authentication a) { return ((JwtUser) a.getPrincipal()).getId(); }
    private AssetResponseDTO asset(Asset a) { return new AssetResponseDTO(a.getId(), a.getName(), a.getSymbol(), a.getType(), a.getSectorId(), a.getCurrency(), a.isActive(), a.getCreatedAt(), a.getModifiedAt()); }
    private ActivityResponseDTO activity(Activity a) { return new ActivityResponseDTO(a.getId(), a.getType(), a.getAssetId(), a.getOccurredAt(), a.getOrderingKey(), a.getQuantity(), a.getUnitPrice(), a.getAmount(), a.getCurrency(), a.getSecondaryAmount(), a.getSecondaryCurrency(), a.getRatio(), a.getLocalCurrency(), a.getFxRateToLocal(), a.getFxRateSource(), a.getFxRateTimestamp(), a.getAmountInLocal(), a.getComment(), a.getLinkedTransactionId()); }
    private HoldingResponseDTO holding(Holding h) { return new HoldingResponseDTO(h.id(), h.assetId(), h.snapshotAt(), h.quantity(), h.acquisitionCost(), h.currency(), h.historyIncomplete(), h.superseded(), h.createdAt()); }
    private PositionResponseDTO position(InvestmentUseCases.PositionView p) { return new PositionResponseDTO(p.assetId(), p.assetName(), p.symbol(), p.assetCurrency(), p.quantity(), p.costBasis(), p.price(), p.marketValue(), p.unrealizedGain(), p.valuationCurrency(), p.fxRateToLocal(), p.priceTimestamp(), p.priceSource(), p.fxTimestamp(), p.fxSource(), p.estimated(), p.historyIncomplete(), p.valuationStatus(), p.unavailableReason()); }
    private PortfolioResponseDTO portfolio(InvestmentUseCases.PortfolioView p) { return new PortfolioResponseDTO(p.asOf(), p.cashBalances(), p.positions().stream().map(this::position).toList(), p.totalInLocalCurrency(), p.localCurrency(), p.estimated(), p.valuationStatus(), p.historyIncomplete(), p.cashValuations().stream().map(c -> new CashValuationResponseDTO(c.currency(), c.balance(), c.fxRateToLocal(), c.amountInLocalCurrency(), c.fxTimestamp(), c.fxSource(), c.valuationStatus())).toList()); }
    private AssetPriceResponseDTO price(AssetPrice p) { return new AssetPriceResponseDTO(p.id(), p.assetId(), p.timestamp(), p.open(), p.high(), p.low(), p.close(), p.adjustedClose(), p.valuationPrice(), p.volume(), p.currency(), p.source(), p.fetchedAt()); }
    private FxRateResponseDTO fxRate(FxRate r) { return new FxRateResponseDTO(r.id(), r.baseCurrency(), r.quoteCurrency(), r.timestamp(), r.rate(), r.source(), r.fetchedAt()); }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) throw new InvalidInputException("page debe ser >= 0 y size entre 1 y 200.");
    }
    private <T> PaginatedResponseDTO<T> page(List<T> all, int requestedPage, int size) {
        int totalPages = (int) Math.ceil((double) all.size() / size);
        int page = Math.min(requestedPage, Math.max(0, totalPages - 1));
        int start = Math.min(page * size, all.size());
        int end = Math.min(start + size, all.size());
        return new PaginatedResponseDTO<>(all.subList(start, end), page, totalPages, all.size(), size,
                page == 0, page >= totalPages - 1 || totalPages == 0);
    }
}
