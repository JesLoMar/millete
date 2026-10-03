package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.HoldingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Representación portable del ledger de Investments utilizada
 * por el formato de exportación/importación de DataExport.
 *
 * Mantiene la estructura portable del nuevo Investments bounded context,
 * pero utiliza JsonNode para los detalles polimórficos de Activity.
 */
@JsonDeserialize(using = InvestmentLedgerSnapshotDeserializer.class)
public record InvestmentLedgerSnapshot(
        int version,
        Instant trackingStartAt,
        List<UserAssetSnapshot> userAssets,
        List<UserAssetPriceSnapshot> userAssetPrices,
        List<HoldingSnapshot> holdings,
        List<ActivitySnapshot> activities,
        List<ActivityAuditSnapshot> audits,

        @JsonIgnore
        int legacyRecordsSkipped
) {

    public InvestmentLedgerSnapshot(
            int version,
            Instant trackingStartAt,
            List<UserAssetSnapshot> userAssets,
            List<UserAssetPriceSnapshot> userAssetPrices,
            List<HoldingSnapshot> holdings,
            List<ActivitySnapshot> activities,
            List<ActivityAuditSnapshot> audits
    ) {
        this(
                version,
                trackingStartAt,
                userAssets,
                userAssetPrices,
                holdings,
                activities,
                audits,
                0
        );
    }

    public static InvestmentLedgerSnapshot emptyLegacy(
            int count
    ) {
        return new InvestmentLedgerSnapshot(
                0,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                count
        );
    }

    public record AssetReferenceSnapshot(
            AssetReferenceKind kind,
            UUID userAssetId,
            String sharedAssetStableCatalogId
    ) {
    }

    public record MoneySnapshot(
            BigDecimal amount,
            String currency
    ) {
    }

    public record AppliedFxRateSnapshot(
            String baseCurrency,
            String quoteCurrency,
            BigDecimal rate,
            String source,
            Instant timestamp
    ) {
    }

    public record AssetSectorSnapshot(
            String code,
            String displayName,
            boolean custom
    ) {
    }

    public record UserAssetSnapshot(
            UUID id,
            String name,
            AssetType type,
            AssetSectorSnapshot sector,
            String origin,
            String currency,
            Instant createdAt,
            Instant modifiedAt
    ) {
    }

    public record UserAssetPriceSnapshot(
            UUID id,
            UUID userAssetId,
            MoneySnapshot unitPrice,
            Instant timestamp
    ) {
    }

    public record HoldingSnapshot(
            UUID id,
            AssetReferenceSnapshot assetReference,
            Instant snapshotAt,
            BigDecimal quantity,
            MoneySnapshot acquisitionCost,
            HoldingStatus status,
            Instant createdAt,
            Instant supersededAt
    ) {
    }

    public record ActivitySnapshot(
            UUID id,
            ActivityType type,
            AssetReferenceSnapshot assetReference,
            Instant occurredAt,
            long sequence,
            JsonNode details,
            String comment,
            Instant createdAt,
            Instant modifiedAt
    ) {
    }

    public record ActivityAuditSnapshot(
            UUID id,
            UUID activityId,
            String beforeJson,
            String afterJson,
            String reason,
            Instant changedAt
    ) {
    }
}