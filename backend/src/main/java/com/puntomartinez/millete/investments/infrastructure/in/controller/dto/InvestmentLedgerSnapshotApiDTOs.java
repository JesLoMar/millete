package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.HoldingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class InvestmentLedgerSnapshotApiDTOs {

    private InvestmentLedgerSnapshotApiDTOs() {
    }

    public record InvestmentLedgerSnapshotDTO(
            int version,
            Instant trackingStartAt,
            List<UserAssetSnapshotDTO> userAssets,
            List<UserAssetPriceSnapshotDTO> userAssetPrices,
            List<HoldingSnapshotDTO> holdings,
            List<ActivitySnapshotDTO> activities,
            List<ActivityAuditSnapshotDTO> audits
    ) {
    }

    public record AssetReferenceSnapshotDTO(
            AssetReferenceKind kind,
            UUID userAssetId,
            String sharedAssetStableCatalogId
    ) {
    }

    public record MoneySnapshotDTO(
            BigDecimal amount,
            String currency
    ) {
    }

    public record AppliedFxRateSnapshotDTO(
            String baseCurrency,
            String quoteCurrency,
            BigDecimal rate,
            String source,
            Instant timestamp
    ) {
    }

    public record AssetSectorSnapshotDTO(
            String code,
            String displayName,
            boolean custom
    ) {
    }

    public record UserAssetSnapshotDTO(
            UUID id,
            String name,
            AssetType type,
            AssetSectorSnapshotDTO sector,
            String origin,
            String currency,
            Instant createdAt,
            Instant modifiedAt
    ) {
    }

    public record UserAssetPriceSnapshotDTO(
            UUID id,
            UUID userAssetId,
            MoneySnapshotDTO unitPrice,
            Instant timestamp
    ) {
    }

    public record HoldingSnapshotDTO(
            UUID id,
            AssetReferenceSnapshotDTO assetReference,
            Instant snapshotAt,
            BigDecimal quantity,
            MoneySnapshotDTO acquisitionCost,
            HoldingStatus status,
            Instant createdAt,
            Instant supersededAt
    ) {
    }

    public record ActivitySnapshotDTO(
            UUID id,
            ActivityType type,
            AssetReferenceSnapshotDTO assetReference,
            Instant occurredAt,
            long sequence,
            JsonNode details,
            String comment,
            Instant createdAt,
            Instant modifiedAt
    ) {
    }

    public record ActivityAuditSnapshotDTO(
            UUID id,
            UUID activityId,
            String beforeJson,
            String afterJson,
            String reason,
            Instant changedAt
    ) {
    }
}