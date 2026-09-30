package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityAudit;
import com.puntomartinez.millete.investments.domain.model.ActivityDetails;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AppliedFxRate;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.OpeningPositionDetails;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentLedgerSnapshotUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityAuditRepository;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.HoldingRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentTrackingSettingsRepository;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InvestmentLedgerSnapshotService
        implements GetInvestmentLedgerSnapshotUseCase {

    private static final int CURRENT_SNAPSHOT_VERSION =
            InvestmentLedgerSnapshot.CURRENT_VERSION;

    private final ActivityRepository activities;
    private final ActivityAuditRepository audits;
    private final HoldingRepository holdings;
    private final UserAssetRepository userAssets;
    private final UserAssetPriceRepository userAssetPrices;
    private final SharedAssetRepository sharedAssets;
    private final InvestmentTrackingSettingsRepository trackingSettings;

    public InvestmentLedgerSnapshotService(
            ActivityRepository activities,
            ActivityAuditRepository audits,
            HoldingRepository holdings,
            UserAssetRepository userAssets,
            UserAssetPriceRepository userAssetPrices,
            SharedAssetRepository sharedAssets,
            InvestmentTrackingSettingsRepository trackingSettings
    ) {
        this.activities = activities;
        this.audits = audits;
        this.holdings = holdings;
        this.userAssets = userAssets;
        this.userAssetPrices = userAssetPrices;
        this.sharedAssets = sharedAssets;
        this.trackingSettings = trackingSettings;
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentLedgerSnapshot get(
            UUID userId
    ) {
        validateUserId(userId);

        Instant trackingStartAt =
                trackingSettings
                        .findTrackingStartAt(userId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El tracking de Investments no está configurado."
                                )
                        );

        List<UserAsset> userAssetList =
                userAssets.findAllByUserId(
                        userId,
                        null
                );

        List<UserAssetPrice> userAssetPriceList =
                userAssetPrices.findAllByUserId(
                        userId
                );

        List<Holding> holdingList =
                holdings.findAllByUserId(
                        userId
                );

        List<Activity> activityList =
                activities.findAllByUserId(
                        userId
                );

        List<ActivityAudit> auditList =
                audits.findAllByUserId(
                        userId
                );

        Map<UUID, String> sharedCatalogIds =
                resolveSharedCatalogIds(
                        activityList,
                        holdingList
                );

        List<InvestmentLedgerSnapshot.UserAssetSnapshot>
                userAssetSnapshots =
                userAssetList
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        UserAsset::getId
                                )
                        )
                        .map(
                                this::toUserAssetSnapshot
                        )
                        .toList();

        List<InvestmentLedgerSnapshot.UserAssetPriceSnapshot>
                userAssetPriceSnapshots =
                userAssetPriceList
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                UserAssetPrice::getTimestamp
                                        )
                                        .thenComparing(
                                                UserAssetPrice::getId
                                        )
                        )
                        .map(
                                this::toUserAssetPriceSnapshot
                        )
                        .toList();

        List<InvestmentLedgerSnapshot.HoldingSnapshot>
                holdingSnapshots =
                holdingList
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                Holding::getSnapshotAt
                                        )
                                        .thenComparing(
                                                Holding::getId
                                        )
                        )
                        .map(
                                holding ->
                                        toHoldingSnapshot(
                                                holding,
                                                sharedCatalogIds
                                        )
                        )
                        .toList();

        List<InvestmentLedgerSnapshot.ActivitySnapshot>
                activitySnapshots =
                buildActivitySnapshots(
                        activityList,
                        sharedCatalogIds
                );

        List<InvestmentLedgerSnapshot.ActivityAuditSnapshot>
                auditSnapshots =
                auditList
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                ActivityAudit::changedAt
                                        )
                                        .thenComparing(
                                                ActivityAudit::id
                                        )
                        )
                        .map(
                                this::toAuditSnapshot
                        )
                        .toList();

        return new InvestmentLedgerSnapshot(
                CURRENT_SNAPSHOT_VERSION,
                trackingStartAt,
                userAssetSnapshots,
                userAssetPriceSnapshots,
                holdingSnapshots,
                activitySnapshots,
                auditSnapshots
        );
    }

    private List<InvestmentLedgerSnapshot.ActivitySnapshot>
    buildActivitySnapshots(
            List<Activity> activities,
            Map<UUID, String> sharedCatalogIds
    ) {
        List<Activity> orderedActivities =
                activities.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                Activity::getOccurredAt
                                        )
                                        .thenComparingLong(
                                                Activity::getOrderingKey
                                        )
                                        .thenComparing(
                                                Activity::getId
                                        )
                        )
                        .toList();

        List<InvestmentLedgerSnapshot.ActivitySnapshot>
                result =
                new ArrayList<>(
                        orderedActivities.size()
                );

        for (int index = 0;
             index < orderedActivities.size();
             index++) {

            Activity activity =
                    orderedActivities.get(index);

            result.add(
                    toActivitySnapshot(
                            activity,
                            sharedCatalogIds,
                            index
                    )
            );
        }

        return List.copyOf(result);
    }

    private InvestmentLedgerSnapshot.UserAssetSnapshot
    toUserAssetSnapshot(
            UserAsset asset
    ) {
        return new InvestmentLedgerSnapshot.UserAssetSnapshot(
                asset.getId(),
                asset.getName(),
                asset.getType(),
                toSectorSnapshot(
                        asset.getSector()
                ),
                asset.getOrigin().name(),
                asset.getCurrency().value(),
                asset.getCreatedAt(),
                asset.getModifiedAt()
        );
    }

    private InvestmentLedgerSnapshot.UserAssetPriceSnapshot
    toUserAssetPriceSnapshot(
            UserAssetPrice price
    ) {
        return new InvestmentLedgerSnapshot.UserAssetPriceSnapshot(
                price.getId(),
                price.getUserAssetId(),
                toMoneySnapshot(
                        price.getUnitPrice()
                ),
                price.getTimestamp()
        );
    }

    private InvestmentLedgerSnapshot.HoldingSnapshot
    toHoldingSnapshot(
            Holding holding,
            Map<UUID, String> sharedCatalogIds
    ) {
        return new InvestmentLedgerSnapshot.HoldingSnapshot(
                holding.getId(),
                toAssetReferenceSnapshot(
                        holding.getAssetReference(),
                        sharedCatalogIds
                ),
                holding.getSnapshotAt(),
                holding.getQuantity(),
                toMoneySnapshot(
                        holding.getAcquisitionCost()
                ),
                holding.getStatus(),
                holding.getCreatedAt(),
                holding.getSupersededAt()
        );
    }

    private InvestmentLedgerSnapshot.ActivitySnapshot
    toActivitySnapshot(
            Activity activity,
            Map<UUID, String> sharedCatalogIds,
            long sequence
    ) {
        return new InvestmentLedgerSnapshot.ActivitySnapshot(
                activity.getId(),
                activity.getType(),
                toAssetReferenceSnapshot(
                        activity.getAssetReference(),
                        sharedCatalogIds
                ),
                activity.getOccurredAt(),
                sequence,
                toActivityDetailsSnapshot(
                        activity.getType(),
                        activity.getDetails()
                ),
                activity.getComment(),
                activity.getCreatedAt(),
                activity.getModifiedAt()
        );
    }

    private InvestmentLedgerSnapshot.ActivityAuditSnapshot
    toAuditSnapshot(
            ActivityAudit audit
    ) {
        return new InvestmentLedgerSnapshot.ActivityAuditSnapshot(
                audit.id(),
                audit.activityId(),
                audit.beforeJson(),
                audit.afterJson(),
                audit.reason(),
                audit.changedAt()
        );
    }

    private InvestmentLedgerSnapshot.AssetReferenceSnapshot
    toAssetReferenceSnapshot(
            AssetReference reference,
            Map<UUID, String> sharedCatalogIds
    ) {
        if (reference == null) {
            return null;
        }

        if (reference.kind() == AssetReferenceKind.USER) {
            return InvestmentLedgerSnapshot
                    .AssetReferenceSnapshot
                    .user(
                            reference.id()
                    );
        }

        String stableCatalogId =
                sharedCatalogIds.get(
                        reference.id()
                );

        if (stableCatalogId == null) {
            throw new IllegalStateException(
                    "No se encontró stableCatalogId para el SharedAsset "
                            + reference.id()
            );
        }

        return InvestmentLedgerSnapshot
                .AssetReferenceSnapshot
                .shared(
                        stableCatalogId
                );
    }

    private InvestmentLedgerSnapshot.AssetSectorSnapshot
    toSectorSnapshot(
            AssetSector sector
    ) {
        return new InvestmentLedgerSnapshot.AssetSectorSnapshot(
                sector.code(),
                sector.displayName(),
                sector.custom()
        );
    }

    private InvestmentLedgerSnapshot.MoneySnapshot
    toMoneySnapshot(
            Money money
    ) {
        return new InvestmentLedgerSnapshot.MoneySnapshot(
                money.amount(),
                money.currency().value()
        );
    }

    private InvestmentLedgerSnapshot.AppliedFxRateSnapshot
    toAppliedFxRateSnapshot(
            AppliedFxRate fx
    ) {
        if (fx == null) {
            return null;
        }

        return new InvestmentLedgerSnapshot.AppliedFxRateSnapshot(
                fx.baseCurrency().value(),
                fx.quoteCurrency().value(),
                fx.rate(),
                fx.source(),
                fx.timestamp()
        );
    }

    private InvestmentLedgerSnapshot.ActivityDetailsSnapshot
    toActivityDetailsSnapshot(
            ActivityType type,
            ActivityDetails details
    ) {
        return switch (type) {

            case BUY, SELL -> {
                if (!(details instanceof TradeActivityDetails trade)) {
                    throw new IllegalStateException(
                            "Los detalles de "
                                    + type
                                    + " no son TradeActivityDetails."
                    );
                }

                yield new InvestmentLedgerSnapshot
                        .TradeDetailsSnapshot(
                                trade.quantity(),
                                toMoneySnapshot(
                                        trade.unitPrice()
                                ),
                                toMoneySnapshot(
                                        trade.settlement()
                                                .amount()
                                ),
                                toAppliedFxRateSnapshot(
                                        trade.settlement()
                                                .appliedFxRate()
                                )
                        );
            }

            case DIVIDEND -> {
                if (details instanceof CashActivityDetails cash) {

                    yield new InvestmentLedgerSnapshot
                            .CashDetailsSnapshot(
                                    toMoneySnapshot(
                                            cash.amount()
                                    )
                            );
                }

                if (details instanceof DividendUnitsDetails units) {

                    yield new InvestmentLedgerSnapshot
                            .DividendUnitsDetailsSnapshot(
                                    units.quantity(),
                                    toMoneySnapshot(
                                            units.referenceUnitPrice()
                                    )
                            );
                }

                throw new IllegalStateException(
                        "Los detalles de DIVIDEND no son válidos."
                );
            }

            case DEPOSIT,
                    WITHDRAW,
                    OPENING_CASH -> {

                if (!(details instanceof CashActivityDetails cash)) {
                    throw new IllegalStateException(
                            "Los detalles de "
                                    + type
                                    + " no son CashActivityDetails."
                    );
                }

                yield new InvestmentLedgerSnapshot
                        .CashDetailsSnapshot(
                                toMoneySnapshot(
                                        cash.amount()
                                )
                        );
            }

            case EXCHANGE -> {

                if (!(details instanceof ExchangeActivityDetails exchange)) {
                    throw new IllegalStateException(
                            "Los detalles de EXCHANGE no son "
                                    + "ExchangeActivityDetails."
                    );
                }

                yield new InvestmentLedgerSnapshot
                        .ExchangeDetailsSnapshot(
                                toMoneySnapshot(
                                        exchange.origin()
                                ),
                                toMoneySnapshot(
                                        exchange.destination()
                                ),
                                toAppliedFxRateSnapshot(
                                        exchange.appliedFxRate()
                                )
                        );
            }

            case SPLIT -> {

                if (!(details instanceof SplitActivityDetails split)) {
                    throw new IllegalStateException(
                            "Los detalles de SPLIT no son "
                                    + "SplitActivityDetails."
                    );
                }

                yield new InvestmentLedgerSnapshot
                        .SplitDetailsSnapshot(
                                split.ratio()
                        );
            }

            case OPENING_POSITION -> {

                if (!(details instanceof OpeningPositionDetails opening)) {
                    throw new IllegalStateException(
                            "Los detalles de OPENING_POSITION no son "
                                    + "OpeningPositionDetails."
                    );
                }

                yield new InvestmentLedgerSnapshot
                        .OpeningPositionDetailsSnapshot(
                                opening.quantity(),
                                toMoneySnapshot(
                                        opening.acquisitionCost()
                                )
                        );
            }
        };
    }

    private Map<UUID, String> resolveSharedCatalogIds(
            List<Activity> activities,
            List<Holding> holdings
    ) {
        Map<UUID, String> result =
                new HashMap<>();

        List<AssetReference> references =
                new ArrayList<>(
                        activities.size()
                                + holdings.size()
                );

        activities.stream()
                .map(Activity::getAssetReference)
                .filter(reference -> reference != null)
                .forEach(references::add);

        holdings.stream()
                .map(Holding::getAssetReference)
                .filter(reference -> reference != null)
                .forEach(references::add);

        references.stream()
                .filter(reference ->
                        reference.kind()
                                == AssetReferenceKind.SHARED
                )
                .map(AssetReference::id)
                .distinct()
                .forEach(sharedAssetId -> {

                    SharedAsset asset =
                            sharedAssets
                                    .findById(
                                            sharedAssetId
                                    )
                                    .orElseThrow(() ->
                                            new IllegalStateException(
                                                    "SharedAsset no encontrado: "
                                                            + sharedAssetId
                                            )
                                    );

                    result.put(
                            sharedAssetId,
                            asset.getStableCatalogId()
                    );
                });

        return Map.copyOf(result);
    }

    private void validateUserId(
            UUID userId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }
    }
}