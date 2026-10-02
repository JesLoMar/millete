package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentLedgerSnapshotApiDTOs;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvestmentLedgerSnapshotMapper {

    private final ObjectMapper objectMapper;

    public InvestmentLedgerSnapshotMapper(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public InvestmentLedgerSnapshotApiDTOs.InvestmentLedgerSnapshotDTO toResponse(
            InvestmentLedgerSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshotApiDTOs.InvestmentLedgerSnapshotDTO(
                snapshot.version(),
                snapshot.trackingStartAt(),
                snapshot.userAssets()
                        .stream()
                        .map(this::toUserAssetSnapshot)
                        .toList(),
                snapshot.userAssetPrices()
                        .stream()
                        .map(this::toUserAssetPriceSnapshot)
                        .toList(),
                snapshot.holdings()
                        .stream()
                        .map(this::toHoldingSnapshot)
                        .toList(),
                snapshot.activities()
                        .stream()
                        .map(this::toActivitySnapshot)
                        .toList(),
                snapshot.audits()
                        .stream()
                        .map(this::toAuditSnapshot)
                        .toList()
        );
    }

    public InvestmentLedgerSnapshot toDomain(
            InvestmentLedgerSnapshotApiDTOs.InvestmentLedgerSnapshotDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "El snapshot es obligatorio."
            );
        }

        return new InvestmentLedgerSnapshot(
                dto.version(),
                dto.trackingStartAt(),
                mapUserAssets(dto.userAssets()),
                mapUserAssetPrices(dto.userAssetPrices()),
                mapHoldings(dto.holdings()),
                mapActivities(dto.activities()),
                mapAudits(dto.audits())
        );
    }

    private InvestmentLedgerSnapshot.UserAssetSnapshot
    toDomainUserAssetSnapshot(
            InvestmentLedgerSnapshotApiDTOs.UserAssetSnapshotDTO dto
    ) {
        return new InvestmentLedgerSnapshot.UserAssetSnapshot(
                dto.id(),
                dto.name(),
                dto.type(),
                toDomainSectorSnapshot(dto.sector()),
                dto.origin(),
                dto.currency(),
                dto.createdAt(),
                dto.modifiedAt()
        );
    }

    private InvestmentLedgerSnapshot.UserAssetPriceSnapshot
    toDomainUserAssetPriceSnapshot(
            InvestmentLedgerSnapshotApiDTOs.UserAssetPriceSnapshotDTO dto
    ) {
        return new InvestmentLedgerSnapshot.UserAssetPriceSnapshot(
                dto.id(),
                dto.userAssetId(),
                toDomainMoneySnapshot(dto.unitPrice()),
                dto.timestamp()
        );
    }

    private InvestmentLedgerSnapshot.HoldingSnapshot
    toDomainHoldingSnapshot(
            InvestmentLedgerSnapshotApiDTOs.HoldingSnapshotDTO dto
    ) {
        return new InvestmentLedgerSnapshot.HoldingSnapshot(
                dto.id(),
                toDomainAssetReferenceSnapshot(dto.assetReference()),
                dto.snapshotAt(),
                dto.quantity(),
                toDomainMoneySnapshot(dto.acquisitionCost()),
                dto.status(),
                dto.createdAt(),
                dto.supersededAt()
        );
    }

    private InvestmentLedgerSnapshot.ActivitySnapshot
    toDomainActivitySnapshot(
            InvestmentLedgerSnapshotApiDTOs.ActivitySnapshotDTO dto
    ) {
        return new InvestmentLedgerSnapshot.ActivitySnapshot(
                dto.id(),
                dto.type(),
                toDomainAssetReferenceSnapshot(dto.assetReference()),
                dto.occurredAt(),
                dto.sequence(),
                toDomainActivityDetailsSnapshot(
                        dto.type(),
                        dto.details()
                ),
                dto.comment(),
                dto.createdAt(),
                dto.modifiedAt()
        );
    }

    private InvestmentLedgerSnapshot.ActivityAuditSnapshot
    toDomainAuditSnapshot(
            InvestmentLedgerSnapshotApiDTOs.ActivityAuditSnapshotDTO dto
    ) {
        return new InvestmentLedgerSnapshot.ActivityAuditSnapshot(
                dto.id(),
                dto.activityId(),
                dto.beforeJson(),
                dto.afterJson(),
                dto.reason(),
                dto.changedAt()
        );
    }

    private InvestmentLedgerSnapshot.AssetReferenceSnapshot
    toDomainAssetReferenceSnapshot(
            InvestmentLedgerSnapshotApiDTOs.AssetReferenceSnapshotDTO dto
    ) {
        if (dto == null) {
            return null;
        }

        return new InvestmentLedgerSnapshot.AssetReferenceSnapshot(
                dto.kind(),
                dto.userAssetId(),
                dto.sharedAssetStableCatalogId()
        );
    }

    private InvestmentLedgerSnapshot.MoneySnapshot
    toDomainMoneySnapshot(
            InvestmentLedgerSnapshotApiDTOs.MoneySnapshotDTO dto
    ) {
        if (dto == null) {
            return null;
        }

        return new InvestmentLedgerSnapshot.MoneySnapshot(
                dto.amount(),
                dto.currency()
        );
    }

    private InvestmentLedgerSnapshot.AssetSectorSnapshot
    toDomainSectorSnapshot(
            InvestmentLedgerSnapshotApiDTOs.AssetSectorSnapshotDTO dto
    ) {
        if (dto == null) {
            return null;
        }

        return new InvestmentLedgerSnapshot.AssetSectorSnapshot(
                dto.code(),
                dto.displayName(),
                dto.custom()
        );
    }

    private InvestmentLedgerSnapshot.ActivityDetailsSnapshot
    toDomainActivityDetailsSnapshot(
            ActivityType type,
            JsonNode details
    ) {
        if (details == null || details.isNull()) {
            throw new IllegalArgumentException(
                    "Los detalles de la Activity son obligatorios."
            );
        }

        return switch (type) {
            case BUY, SELL ->
                    treeToValue(
                            details,
                            InvestmentLedgerSnapshot.TradeDetailsSnapshot.class
                    );

            case DIVIDEND -> {
                if (details.has("referenceUnitPrice")) {
                    yield treeToValue(
                            details,
                            InvestmentLedgerSnapshot
                                    .DividendUnitsDetailsSnapshot.class
                    );
                }

                yield treeToValue(
                        details,
                        InvestmentLedgerSnapshot
                                .CashDetailsSnapshot.class
                );
            }

            case DEPOSIT,
                 WITHDRAW,
                 OPENING_CASH ->
                    treeToValue(
                            details,
                            InvestmentLedgerSnapshot
                                    .CashDetailsSnapshot.class
                    );

            case EXCHANGE ->
                    treeToValue(
                            details,
                            InvestmentLedgerSnapshot
                                    .ExchangeDetailsSnapshot.class
                    );

            case SPLIT ->
                    treeToValue(
                            details,
                            InvestmentLedgerSnapshot
                                    .SplitDetailsSnapshot.class
                    );

            case OPENING_POSITION ->
                    treeToValue(
                            details,
                            InvestmentLedgerSnapshot
                                    .OpeningPositionDetailsSnapshot.class
                    );
        };
    }

    private InvestmentLedgerSnapshotApiDTOs.UserAssetSnapshotDTO
    toUserAssetSnapshot(
            InvestmentLedgerSnapshot.UserAssetSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshotApiDTOs.UserAssetSnapshotDTO(
                snapshot.id(),
                snapshot.name(),
                snapshot.type(),
                toSectorSnapshot(snapshot.sector()),
                snapshot.origin(),
                snapshot.currency(),
                snapshot.createdAt(),
                snapshot.modifiedAt()
        );
    }

    private InvestmentLedgerSnapshotApiDTOs.UserAssetPriceSnapshotDTO
    toUserAssetPriceSnapshot(
            InvestmentLedgerSnapshot.UserAssetPriceSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshotApiDTOs.UserAssetPriceSnapshotDTO(
                snapshot.id(),
                snapshot.userAssetId(),
                toMoneySnapshot(snapshot.unitPrice()),
                snapshot.timestamp()
        );
    }

    private InvestmentLedgerSnapshotApiDTOs.HoldingSnapshotDTO
    toHoldingSnapshot(
            InvestmentLedgerSnapshot.HoldingSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshotApiDTOs.HoldingSnapshotDTO(
                snapshot.id(),
                toAssetReferenceSnapshot(snapshot.assetReference()),
                snapshot.snapshotAt(),
                snapshot.quantity(),
                toMoneySnapshot(snapshot.acquisitionCost()),
                snapshot.status(),
                snapshot.createdAt(),
                snapshot.supersededAt()
        );
    }

    private InvestmentLedgerSnapshotApiDTOs.ActivitySnapshotDTO
    toActivitySnapshot(
            InvestmentLedgerSnapshot.ActivitySnapshot snapshot
    ) {
        JsonNode details =
                objectMapper.valueToTree(snapshot.details());

        return new InvestmentLedgerSnapshotApiDTOs.ActivitySnapshotDTO(
                snapshot.id(),
                snapshot.type(),
                toAssetReferenceSnapshot(snapshot.assetReference()),
                snapshot.occurredAt(),
                snapshot.sequence(),
                details,
                snapshot.comment(),
                snapshot.createdAt(),
                snapshot.modifiedAt()
        );
    }

    private InvestmentLedgerSnapshotApiDTOs.ActivityAuditSnapshotDTO
    toAuditSnapshot(
            InvestmentLedgerSnapshot.ActivityAuditSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshotApiDTOs.ActivityAuditSnapshotDTO(
                snapshot.id(),
                snapshot.activityId(),
                snapshot.beforeJson(),
                snapshot.afterJson(),
                snapshot.reason(),
                snapshot.changedAt()
        );
    }

    private InvestmentLedgerSnapshotApiDTOs.AssetReferenceSnapshotDTO
    toAssetReferenceSnapshot(
            InvestmentLedgerSnapshot.AssetReferenceSnapshot snapshot
    ) {
        if (snapshot == null) {
            return null;
        }

        return new InvestmentLedgerSnapshotApiDTOs.AssetReferenceSnapshotDTO(
                snapshot.kind(),
                snapshot.userAssetId(),
                snapshot.sharedAssetStableCatalogId()
        );
    }

    private InvestmentLedgerSnapshotApiDTOs.MoneySnapshotDTO
    toMoneySnapshot(
            InvestmentLedgerSnapshot.MoneySnapshot snapshot
    ) {
        if (snapshot == null) {
            return null;
        }

        return new InvestmentLedgerSnapshotApiDTOs.MoneySnapshotDTO(
                snapshot.amount(),
                snapshot.currency()
        );
    }

    private InvestmentLedgerSnapshotApiDTOs.AssetSectorSnapshotDTO
    toSectorSnapshot(
            InvestmentLedgerSnapshot.AssetSectorSnapshot snapshot
    ) {
        if (snapshot == null) {
            return null;
        }

        return new InvestmentLedgerSnapshotApiDTOs.AssetSectorSnapshotDTO(
                snapshot.code(),
                snapshot.displayName(),
                snapshot.custom()
        );
    }

    private List<InvestmentLedgerSnapshot.UserAssetSnapshot>
    mapUserAssets(
            List<InvestmentLedgerSnapshotApiDTOs.UserAssetSnapshotDTO> values
    ) {
        if (values == null) {
            return null;
        }

        return values.stream()
                .map(this::toDomainUserAssetSnapshot)
                .toList();
    }

    private List<InvestmentLedgerSnapshot.UserAssetPriceSnapshot>
    mapUserAssetPrices(
            List<InvestmentLedgerSnapshotApiDTOs.UserAssetPriceSnapshotDTO> values
    ) {
        if (values == null) {
            return null;
        }

        return values.stream()
                .map(this::toDomainUserAssetPriceSnapshot)
                .toList();
    }

    private List<InvestmentLedgerSnapshot.HoldingSnapshot>
    mapHoldings(
            List<InvestmentLedgerSnapshotApiDTOs.HoldingSnapshotDTO> values
    ) {
        if (values == null) {
            return null;
        }

        return values.stream()
                .map(this::toDomainHoldingSnapshot)
                .toList();
    }

    private List<InvestmentLedgerSnapshot.ActivitySnapshot>
    mapActivities(
            List<InvestmentLedgerSnapshotApiDTOs.ActivitySnapshotDTO> values
    ) {
        if (values == null) {
            return null;
        }

        return values.stream()
                .map(this::toDomainActivitySnapshot)
                .toList();
    }

    private List<InvestmentLedgerSnapshot.ActivityAuditSnapshot>
    mapAudits(
            List<InvestmentLedgerSnapshotApiDTOs.ActivityAuditSnapshotDTO> values
    ) {
        if (values == null) {
            return null;
        }

        return values.stream()
                .map(this::toDomainAuditSnapshot)
                .toList();
    }

    private <T> T treeToValue(
            JsonNode details,
            Class<T> type
    ) {
        try {
            return objectMapper.treeToValue(
                    details,
                    type
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    "No se pudieron deserializar los detalles de la Activity.",
                    exception
            );
        }
    }
}