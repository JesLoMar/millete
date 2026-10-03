package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentImportPort;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.ActivityAuditSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.ActivitySnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.AppliedFxRateSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.AssetReferenceSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.AssetSectorSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.HoldingSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.MoneySnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.UserAssetPriceSnapshot;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.UserAssetSnapshot;
import com.puntomartinez.millete.investments.domain.ports.in.RestoreInvestmentLedgerUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class InvestmentImportAdapter
        implements InvestmentImportPort {

    private final RestoreInvestmentLedgerUseCase restoreLedger;
    private final ObjectMapper objectMapper;

    public InvestmentImportAdapter(
            RestoreInvestmentLedgerUseCase restoreLedger,
            ObjectMapper objectMapper
    ) {
        this.restoreLedger = restoreLedger;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public int importInvestments(
            InvestmentLedgerSnapshot data,
            UUID userId
    ) {
        if (data == null) {
            return 0;
        }

        if (data.legacyRecordsSkipped() > 0) {
            return 0;
        }

        com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
                snapshot =
                toDomain(data);

        restoreLedger.restore(
                userId,
                snapshot
        );

        return data.userAssets().size()
                + data.userAssetPrices().size()
                + data.holdings().size()
                + data.activities().size()
                + data.audits().size();
    }

    private com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
    toDomain(
            InvestmentLedgerSnapshot snapshot
    ) {
        return new com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot(
                snapshot.version(),
                snapshot.trackingStartAt(),
                mapUserAssets(snapshot.userAssets()),
                mapUserAssetPrices(snapshot.userAssetPrices()),
                mapHoldings(snapshot.holdings()),
                mapActivities(snapshot.activities()),
                mapAudits(snapshot.audits())
        );
    }

    private List<UserAssetSnapshot> mapUserAssets(
            List<InvestmentLedgerSnapshot.UserAssetSnapshot> values
    ) {
        return values.stream()
                .map(this::toDomainUserAsset)
                .toList();
    }

    private UserAssetSnapshot toDomainUserAsset(
            InvestmentLedgerSnapshot.UserAssetSnapshot value
    ) {
        return new UserAssetSnapshot(
                value.id(),
                value.name(),
                value.type(),
                toDomainSector(value.sector()),
                value.origin(),
                value.currency(),
                value.createdAt(),
                value.modifiedAt()
        );
    }

    private AssetSectorSnapshot toDomainSector(
            InvestmentLedgerSnapshot.AssetSectorSnapshot value
    ) {
        return new AssetSectorSnapshot(
                value.code(),
                value.displayName(),
                value.custom()
        );
    }

    private List<UserAssetPriceSnapshot> mapUserAssetPrices(
            List<InvestmentLedgerSnapshot.UserAssetPriceSnapshot> values
    ) {
        return values.stream()
                .map(this::toDomainUserAssetPrice)
                .toList();
    }

    private UserAssetPriceSnapshot toDomainUserAssetPrice(
            InvestmentLedgerSnapshot.UserAssetPriceSnapshot value
    ) {
        return new UserAssetPriceSnapshot(
                value.id(),
                value.userAssetId(),
                toDomainMoney(value.unitPrice()),
                value.timestamp()
        );
    }

    private List<HoldingSnapshot> mapHoldings(
            List<InvestmentLedgerSnapshot.HoldingSnapshot> values
    ) {
        return values.stream()
                .map(this::toDomainHolding)
                .toList();
    }

    private HoldingSnapshot toDomainHolding(
            InvestmentLedgerSnapshot.HoldingSnapshot value
    ) {
        return new HoldingSnapshot(
                value.id(),
                toDomainAssetReference(
                        value.assetReference()
                ),
                value.snapshotAt(),
                value.quantity(),
                toDomainMoney(value.acquisitionCost()),
                value.status(),
                value.createdAt(),
                value.supersededAt()
        );
    }

    private List<ActivitySnapshot> mapActivities(
            List<InvestmentLedgerSnapshot.ActivitySnapshot> values
    ) {
        return values.stream()
                .map(this::toDomainActivity)
                .toList();
    }

    private ActivitySnapshot toDomainActivity(
            InvestmentLedgerSnapshot.ActivitySnapshot value
    ) {
        return new ActivitySnapshot(
                value.id(),
                value.type(),
                toDomainAssetReference(
                        value.assetReference()
                ),
                value.occurredAt(),
                value.sequence(),
                toDomainDetails(
                        value.type(),
                        value.details()
                ),
                value.comment(),
                value.createdAt(),
                value.modifiedAt()
        );
    }

    private List<ActivityAuditSnapshot> mapAudits(
            List<InvestmentLedgerSnapshot.ActivityAuditSnapshot> values
    ) {
        return values.stream()
                .map(this::toDomainAudit)
                .toList();
    }

    private ActivityAuditSnapshot toDomainAudit(
            InvestmentLedgerSnapshot.ActivityAuditSnapshot value
    ) {
        return new ActivityAuditSnapshot(
                value.id(),
                value.activityId(),
                value.beforeJson(),
                value.afterJson(),
                value.reason(),
                value.changedAt()
        );
    }

    private AssetReferenceSnapshot toDomainAssetReference(
            InvestmentLedgerSnapshot.AssetReferenceSnapshot value
    ) {
        if (value == null) {
            return null;
        }

        return new AssetReferenceSnapshot(
                value.kind(),
                value.userAssetId(),
                value.sharedAssetStableCatalogId()
        );
    }

    private MoneySnapshot toDomainMoney(
            InvestmentLedgerSnapshot.MoneySnapshot value
    ) {
        if (value == null) {
            return null;
        }

        return new MoneySnapshot(
                value.amount(),
                value.currency()
        );
    }

    private com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.ActivityDetailsSnapshot
    toDomainDetails(
            com.puntomartinez.millete.investments.domain.model.ActivityType type,
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
                            com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot.TradeDetailsSnapshot.class
                    );

            case DIVIDEND -> {
                if (details.has("referenceUnitPrice")) {
                    yield treeToValue(
                            details,
                            com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
                                    .DividendUnitsDetailsSnapshot.class
                    );
                }

                yield treeToValue(
                        details,
                        com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
                                .CashDetailsSnapshot.class
                );
            }

            case DEPOSIT,
                 WITHDRAW,
                 OPENING_CASH ->
                    treeToValue(
                            details,
                            com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
                                    .CashDetailsSnapshot.class
                    );

            case EXCHANGE ->
                    treeToValue(
                            details,
                            com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
                                    .ExchangeDetailsSnapshot.class
                    );

            case SPLIT ->
                    treeToValue(
                            details,
                            com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
                                    .SplitDetailsSnapshot.class
                    );

            case OPENING_POSITION ->
                    treeToValue(
                            details,
                            com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot
                                    .OpeningPositionDetailsSnapshot.class
                    );
        };
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