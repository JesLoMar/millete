package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Inbound contract for the event-based investment portfolio. */
public interface InvestmentUseCases {
    Asset createAsset(UUID userId, CreateAssetCommand command);
    Asset updateAsset(UUID userId, UUID assetId, CreateAssetCommand command);
    void hideAsset(UUID userId, UUID assetId);
    List<Asset> listAssets(UUID userId, boolean includeInactive);
    List<AssetSector> listSectors();
    Activity recordActivity(UUID userId, RecordActivityCommand command);
    Activity editActivity(UUID userId, UUID activityId, EditActivityCommand command);
    Activity editTransferComment(UUID userId, UUID activityId, String comment);
    List<Activity> listActivities(UUID userId, UUID assetId);
    List<ActivityAudit> listActivityAudit(UUID userId, UUID activityId);
    Holding createHolding(UUID userId, CreateHoldingCommand command);
    List<Activity> replaceHoldingWithHistory(UUID userId, UUID holdingId,
                                             List<RecordActivityCommand> history);
    List<Holding> listHoldings(UUID userId, UUID assetId);
    List<AssetPrice> listPrices(UUID userId, UUID assetId, Instant from, Instant to);
    AssetPrice addPrice(UUID userId, AddPriceCommand command);
    FxRate addFxRate(AddFxRateCommand command);
    Map<String, BigDecimal> cashBalances(UUID userId);
    PortfolioView portfolio(UUID userId, Instant at);
    RefreshResult refreshFromProvider(UUID userId, Instant from, Instant to);
    HealthReport health(UUID userId);

    record CreateAssetCommand(String name, String symbol, AssetType type,
                              UUID sectorId, String currency) { }
    record RecordActivityCommand(ActivityType type, Instant occurredAt,
                                 UUID assetId, BigDecimal quantity,
                                 BigDecimal unitPrice, BigDecimal amount,
                                 String currency, BigDecimal secondaryAmount,
                                 String secondaryCurrency, BigDecimal ratio,
                                 BigDecimal exchangeRate, String comment) { }
    record EditActivityCommand(Instant occurredAt, BigDecimal quantity,
                               BigDecimal unitPrice, BigDecimal amount,
                               BigDecimal ratio, String comment,
                               String reason) { }
    record CreateHoldingCommand(UUID assetId, Instant snapshotAt,
                                BigDecimal quantity, BigDecimal acquisitionCost,
                                String currency) { }
    record AddPriceCommand(UUID assetId, Instant timestamp, BigDecimal open,
                           BigDecimal high, BigDecimal low, BigDecimal close,
                           BigDecimal adjustedClose, BigDecimal volume,
                           String currency, String source) { }
    record AddFxRateCommand(String baseCurrency, String quoteCurrency,
                            Instant timestamp, BigDecimal rate, String source) { }
    record PositionView(UUID assetId, String assetName, String symbol,
                        String assetCurrency, BigDecimal quantity,
                        BigDecimal costBasis, BigDecimal price,
                        BigDecimal marketValue, BigDecimal unrealizedGain,
                        String valuationCurrency, Instant priceTimestamp,
                        String priceSource, Instant fxTimestamp, String fxSource,
                        boolean estimated, boolean historyIncomplete) { }
    record PortfolioView(Instant asOf, Map<String, BigDecimal> cashBalances,
                         List<PositionView> positions, BigDecimal totalInLocalCurrency,
                         String localCurrency, boolean estimated) { }
    record RefreshResult(int pricesStored, int fxRatesStored, String provider) { }
    record HealthIssue(String key, String code, String resourceId,
                       String severity, String message) { }
    record HealthReport(Instant checkedAt, List<HealthIssue> issues) { }
}
