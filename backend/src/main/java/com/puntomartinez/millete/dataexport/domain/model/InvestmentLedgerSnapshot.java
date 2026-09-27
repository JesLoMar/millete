package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Version 0.3 investment ledger. Lots and cash balances are derived and deliberately omitted. */
@JsonDeserialize(using = InvestmentLedgerSnapshotDeserializer.class)
public record InvestmentLedgerSnapshot(
        List<AssetSnapshot> assets,
        List<HoldingSnapshot> holdings,
        List<ActivitySnapshot> activities,
        List<ActivityAuditSnapshot> audit,
        List<CurrencyPeriodSnapshot> currencyHistory,
        @JsonIgnore int legacyRecordsSkipped
) {
    public InvestmentLedgerSnapshot(List<AssetSnapshot> assets, List<HoldingSnapshot> holdings,
                                   List<ActivitySnapshot> activities, List<ActivityAuditSnapshot> audit,
                                   List<CurrencyPeriodSnapshot> currencyHistory) {
        this(assets, holdings, activities, audit, currencyHistory, 0);
    }

    public static InvestmentLedgerSnapshot emptyLegacy(int count) {
        return new InvestmentLedgerSnapshot(List.of(), List.of(), List.of(), List.of(), List.of(), count);
    }

    public record AssetSnapshot(UUID id, String name, String symbol, AssetType type, UUID sectorId,
                                String currency, Instant createdAt, Instant modifiedAt, boolean active) { }
    public record HoldingSnapshot(UUID id, UUID assetId, Instant snapshotAt, BigDecimal quantity,
                                  BigDecimal acquisitionCost, String currency, boolean historyIncomplete,
                                  boolean superseded, Instant createdAt) { }
    public record ActivitySnapshot(UUID id, ActivityType type, Instant occurredAt, long orderingKey,
                                   UUID assetId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount,
                                   String currency, BigDecimal secondaryAmount, String secondaryCurrency,
                                   BigDecimal ratio, String localCurrency, BigDecimal fxRateToLocal,
                                   String fxRateSource, Instant fxRateTimestamp, BigDecimal amountInLocal,
                                   String comment, UUID linkedTransactionId, Instant createdAt,
                                   Instant modifiedAt, boolean active) { }
    public record ActivityAuditSnapshot(UUID id, UUID activityId, String beforeJson, String afterJson,
                                        String reason, Instant changedAt) { }
    public record CurrencyPeriodSnapshot(UUID id, String currency, Instant validFrom,
                                         Instant validTo, boolean inferred) { }
}
