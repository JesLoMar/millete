package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentExportPort;
import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityDetails;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.AssetReferenceSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.AssetSectorSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.UserAssetSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.UserAssetPriceSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.HoldingSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.ActivityAuditSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.ActivitySnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.MoneySnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.AppliedFxRateSnapshot;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentLedgerSnapshotUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvestmentExportAdapter
        implements InvestmentExportPort {

    private final GetInvestmentLedgerSnapshotUseCase getSnapshot;
    private final ObjectMapper objectMapper;

    public InvestmentExportAdapter(
            GetInvestmentLedgerSnapshotUseCase getSnapshot,
            ObjectMapper objectMapper
    ) {
        this.getSnapshot = getSnapshot;
        this.objectMapper = objectMapper;
    }

    @Override
    public InvestmentLedgerSnapshot findAllByUserId(
            java.util.UUID userId
    ) {
        com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
                snapshot =
                getSnapshot.get(userId);

        return new InvestmentLedgerSnapshot(
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

    private InvestmentLedgerSnapshot.UserAssetSnapshot
    toUserAssetSnapshot(
            UserAssetSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshot.UserAssetSnapshot(
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

    private InvestmentLedgerSnapshot.AssetSectorSnapshot
    toSectorSnapshot(
            AssetSectorSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshot.AssetSectorSnapshot(
                snapshot.code(),
                snapshot.displayName(),
                snapshot.custom()
        );
    }

    private InvestmentLedgerSnapshot.UserAssetPriceSnapshot
    toUserAssetPriceSnapshot(
            UserAssetPriceSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshot.UserAssetPriceSnapshot(
                snapshot.id(),
                snapshot.userAssetId(),
                toMoneySnapshot(snapshot.unitPrice()),
                snapshot.timestamp()
        );
    }

    private InvestmentLedgerSnapshot.HoldingSnapshot
    toHoldingSnapshot(
            HoldingSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshot.HoldingSnapshot(
                snapshot.id(),
                toAssetReferenceSnapshot(
                        snapshot.assetReference()
                ),
                snapshot.snapshotAt(),
                snapshot.quantity(),
                toMoneySnapshot(
                        snapshot.acquisitionCost()
                ),
                snapshot.status(),
                snapshot.createdAt(),
                snapshot.supersededAt()
        );
    }

    private InvestmentLedgerSnapshot.ActivitySnapshot
    toActivitySnapshot(
            ActivitySnapshot snapshot
    ) {
        JsonNode details =
                objectMapper.valueToTree(
                        snapshot.details()
                );

        return new InvestmentLedgerSnapshot.ActivitySnapshot(
                snapshot.id(),
                snapshot.type(),
                toAssetReferenceSnapshot(
                        snapshot.assetReference()
                ),
                snapshot.occurredAt(),
                snapshot.sequence(),
                details,
                snapshot.comment(),
                snapshot.createdAt(),
                snapshot.modifiedAt()
        );
    }

    private InvestmentLedgerSnapshot.ActivityAuditSnapshot
    toAuditSnapshot(
            ActivityAuditSnapshot snapshot
    ) {
        return new InvestmentLedgerSnapshot.ActivityAuditSnapshot(
                snapshot.id(),
                snapshot.activityId(),
                snapshot.beforeJson(),
                snapshot.afterJson(),
                snapshot.reason(),
                snapshot.changedAt()
        );
    }

    private InvestmentLedgerSnapshot.AssetReferenceSnapshot
    toAssetReferenceSnapshot(
            AssetReferenceSnapshot snapshot
    ) {
        if (snapshot == null) {
            return null;
        }

        return new InvestmentLedgerSnapshot.AssetReferenceSnapshot(
                snapshot.kind(),
                snapshot.userAssetId(),
                snapshot.sharedAssetStableCatalogId()
        );
    }

    private InvestmentLedgerSnapshot.MoneySnapshot
    toMoneySnapshot(
            MoneySnapshot snapshot
    ) {
        if (snapshot == null) {
            return null;
        }

        return new InvestmentLedgerSnapshot.MoneySnapshot(
                snapshot.amount(),
                snapshot.currency()
        );
    }
}