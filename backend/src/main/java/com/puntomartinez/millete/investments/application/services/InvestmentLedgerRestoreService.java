package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityAudit;
import com.puntomartinez.millete.investments.domain.model.ActivityDetails;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AppliedFxRate;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.HoldingStatus;
import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.OpeningPositionDetails;
import com.puntomartinez.millete.investments.domain.model.PortfolioReplay;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeSettlement;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.RestoreInvestmentLedgerUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityAuditRepository;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityOrderingPort;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.AssetSectorCatalogPort;
import com.puntomartinez.millete.investments.domain.ports.out.HoldingRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentTrackingSettingsRepository;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.TransactionIntegrationPort;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class InvestmentLedgerRestoreService
        implements RestoreInvestmentLedgerUseCase {

    private final ActivityRepository activities;
    private final ActivityAuditRepository audits;
    private final HoldingRepository holdings;
    private final UserAssetRepository userAssets;
    private final UserAssetPriceRepository userAssetPrices;
    private final SharedAssetRepository sharedAssets;
    private final AssetSectorCatalogPort sectors;
    private final InvestmentTrackingSettingsRepository trackingSettings;
    private final ActivityOrderingPort ordering;
    private final InvestmentPortfolioLockPort portfolioLock;
    private final TransactionIntegrationPort transactions;
    private final UserCurrencyPort userCurrencies;
    private final PortfolioRebuildService portfolioRebuild;

    public InvestmentLedgerRestoreService(
            ActivityRepository activities,
            ActivityAuditRepository audits,
            HoldingRepository holdings,
            UserAssetRepository userAssets,
            UserAssetPriceRepository userAssetPrices,
            SharedAssetRepository sharedAssets,
            AssetSectorCatalogPort sectors,
            InvestmentTrackingSettingsRepository trackingSettings,
            ActivityOrderingPort ordering,
            InvestmentPortfolioLockPort portfolioLock,
            TransactionIntegrationPort transactions,
            UserCurrencyPort userCurrencies,
            PortfolioRebuildService portfolioRebuild
    ) {
        this.activities = activities;
        this.audits = audits;
        this.holdings = holdings;
        this.userAssets = userAssets;
        this.userAssetPrices = userAssetPrices;
        this.sharedAssets = sharedAssets;
        this.sectors = sectors;
        this.trackingSettings = trackingSettings;
        this.ordering = ordering;
        this.portfolioLock = portfolioLock;
        this.transactions = transactions;
        this.userCurrencies = userCurrencies;
        this.portfolioRebuild = portfolioRebuild;
    }

    @Override
    @Transactional
    public void restore(
            UUID userId,
            InvestmentLedgerSnapshot snapshot
    ) {
        validateUserId(userId);
        validateSnapshot(snapshot);

        portfolioLock.lock(userId);

        Instant existingTrackingStartAt =
                trackingSettings
                        .findTrackingStartAt(userId)
                        .orElse(null);

        if (existingTrackingStartAt != null
                && !existingTrackingStartAt.equals(
                snapshot.trackingStartAt()
        )) {

            throw new InvalidInputException(
                    "El trackingStartAt del snapshot no coincide "
                            + "con el trackingStartAt ya configurado."
            );
        }

        List<Activity> existingActivities =
                activities.findAllByUserId(userId);

        List<Holding> existingHoldings =
                holdings.findAllByUserId(userId);

        RestoreContext context =
                buildRestoreContext(
                        userId,
                        snapshot,
                        existingActivities
                );

        validateActivityAuditReferences(
                snapshot,
                context.activityIdMap()
        );

        validatePortfolioIntegrityBeforePersist(
                userId,
                existingActivities,
                existingHoldings,
                context,
                snapshot.trackingStartAt()
        );

        if (existingTrackingStartAt == null) {
            trackingSettings.saveTrackingStartAt(
                    userId,
                    snapshot.trackingStartAt()
            );
        }

        persistUserAssets(
                context.userAssets()
        );

        persistUserAssetPrices(
                context.userAssetPrices()
        );

        persistHoldings(
                context.holdings()
        );

        persistActivities(
                userId,
                context.activities()
        );

        persistAudits(
                userId,
                snapshot.audit(),
                context.activityIdMap()
        );

        portfolioRebuild.rebuild(userId);
    }

    private RestoreContext buildRestoreContext(
            UUID userId,
            InvestmentLedgerSnapshot snapshot,
            List<Activity> existingActivities
    ) {
        validateUniqueUserAssetIds(
                snapshot.userAssets()
        );

        validateUniqueUserAssetPriceIds(
                snapshot.userAssetPrices()
        );

        validateUniqueHoldingIds(
                snapshot.holdings()
        );

        validateUniqueActivityIds(
                snapshot.activities()
        );

        validateUniqueActivitySequences(
                snapshot.activities()
        );

        validateUniqueAuditIds(
                snapshot.audit()
        );

        Map<UUID, UUID> userAssetIdMap =
                new LinkedHashMap<>();

        Map<UUID, CurrencyCode> userAssetCurrencies =
                new HashMap<>();

        List<UserAsset> restoredUserAssets =
                new ArrayList<>();

        for (InvestmentLedgerSnapshot.UserAssetSnapshot
                assetSnapshot : snapshot.userAssets()) {

            UUID restoredId =
                    UUID.randomUUID();

            AssetSector sector =
                    restoreSector(
                            assetSnapshot.sector()
                    );

            AssetType type =
                    assetSnapshot.type();

            com.puntomartinez.millete.investments.domain.model.AssetOrigin
                    origin =
                    parseAssetOrigin(
                            assetSnapshot.origin()
                    );

            CurrencyCode currency =
                    CurrencyCode.of(
                            assetSnapshot.currency()
                    );

            UserAsset asset =
                    UserAsset.reconstitute(
                            restoredId,
                            userId,
                            assetSnapshot.name(),
                            type,
                            sector,
                            origin,
                            currency,
                            assetSnapshot.createdAt(),
                            assetSnapshot.modifiedAt()
                    );

            restoredUserAssets.add(asset);

            userAssetIdMap.put(
                    assetSnapshot.id(),
                    restoredId
            );

            userAssetCurrencies.put(
                    restoredId,
                    currency
            );
        }

        Map<String, SharedAsset> sharedAssetsByStableId =
                resolveSharedAssets(
                        snapshot,
                        userAssetIdMap
                );

        List<UserAssetPrice> restoredPrices =
                new ArrayList<>();

        for (InvestmentLedgerSnapshot.UserAssetPriceSnapshot
                priceSnapshot : snapshot.userAssetPrices()) {

            UUID restoredUserAssetId =
                    userAssetIdMap.get(
                            priceSnapshot.userAssetId()
                    );

            if (restoredUserAssetId == null) {
                throw new InvalidInputException(
                        "El UserAssetPrice referencia un UserAsset "
                                + "que no existe en el snapshot."
                );
            }

            UUID restoredPriceId =
                    UUID.randomUUID();

            UserAssetPrice price =
                    UserAssetPrice.reconstitute(
                            restoredPriceId,
                            restoredUserAssetId,
                            toMoney(
                                    priceSnapshot.unitPrice()
                            ),
                            priceSnapshot.timestamp()
                    );

            restoredPrices.add(price);
        }

        List<Holding> restoredHoldings =
                new ArrayList<>();

        for (InvestmentLedgerSnapshot.HoldingSnapshot
                holdingSnapshot : snapshot.holdings()) {

            AssetReference assetReference =
                    restoreAssetReference(
                            holdingSnapshot.assetReference(),
                            userAssetIdMap,
                            sharedAssetsByStableId
                    );

            Holding holding =
                    Holding.reconstitute(
                            UUID.randomUUID(),
                            userId,
                            assetReference,
                            holdingSnapshot.snapshotAt(),
                            holdingSnapshot.quantity(),
                            toMoney(
                                    holdingSnapshot.acquisitionCost()
                            ),
                            holdingSnapshot.status(),
                            holdingSnapshot.createdAt(),
                            holdingSnapshot.supersededAt()
                    );

            restoredHoldings.add(holding);
        }

        List<InvestmentLedgerSnapshot.ActivitySnapshot>
                orderedSnapshots =
                snapshot.activities()
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                InvestmentLedgerSnapshot
                                                        .ActivitySnapshot
                                                        ::occurredAt
                                        )
                                        .thenComparingLong(
                                                InvestmentLedgerSnapshot
                                                        .ActivitySnapshot
                                                        ::sequence
                                        )
                                        .thenComparing(
                                                InvestmentLedgerSnapshot
                                                        .ActivitySnapshot
                                                        ::id
                                        )
                        )
                        .toList();

        Map<Instant, Long> nextProvisionalOrdering =
                buildInitialOrderingState(
                        existingActivities
                );

        List<RestoredActivity> restoredActivities =
                new ArrayList<>();

        Map<UUID, UUID> activityIdMap =
                new LinkedHashMap<>();

        for (InvestmentLedgerSnapshot.ActivitySnapshot
                activitySnapshot : orderedSnapshots) {

            AssetReference assetReference =
                    restoreAssetReference(
                            activitySnapshot.assetReference(),
                            userAssetIdMap,
                            sharedAssetsByStableId
                    );

            ActivityDetails details =
                    restoreActivityDetails(
                            activitySnapshot,
                            assetReference,
                            userAssetCurrencies,
                            sharedAssetsByStableId
                    );

            long provisionalOrderingKey =
                    nextProvisionalOrderingKey(
                            nextProvisionalOrdering,
                            activitySnapshot.occurredAt()
                    );

            UUID restoredActivityId =
                    UUID.randomUUID();

            Activity restoredActivity =
                    Activity.reconstitute(
                            restoredActivityId,
                            userId,
                            activitySnapshot.type(),
                            assetReference,
                            activitySnapshot.occurredAt(),
                            activitySnapshot.createdAt(),
                            activitySnapshot.modifiedAt(),
                            provisionalOrderingKey,
                            details,
                            activitySnapshot.comment(),
                            null
                    );

            validateRestoredActivitySemantics(
                    restoredActivity,
                    userId,
                    userAssetCurrencies,
                    sharedAssetsByStableId
            );

            restoredActivities.add(
                    new RestoredActivity(
                            activitySnapshot,
                            restoredActivity
                    )
            );

            activityIdMap.put(
                    activitySnapshot.id(),
                    restoredActivityId
            );
        }

        return new RestoreContext(
                restoredUserAssets,
                restoredPrices,
                restoredHoldings,
                restoredActivities,
                userAssetIdMap,
                activityIdMap
        );
    }

    private Map<String, SharedAsset> resolveSharedAssets(
            InvestmentLedgerSnapshot snapshot,
            Map<UUID, UUID> userAssetIdMap
    ) {
        Set<String> stableCatalogIds =
                new HashSet<>();

        for (InvestmentLedgerSnapshot.ActivitySnapshot activity
                : snapshot.activities()) {

            addSharedCatalogId(
                    stableCatalogIds,
                    activity.assetReference()
            );
        }

        for (InvestmentLedgerSnapshot.HoldingSnapshot holding
                : snapshot.holdings()) {

            addSharedCatalogId(
                    stableCatalogIds,
                    holding.assetReference()
            );
        }

        Map<String, SharedAsset> result =
                new LinkedHashMap<>();

        for (String stableCatalogId : stableCatalogIds) {

            SharedAsset asset =
                    sharedAssets
                            .findByStableCatalogId(
                                    stableCatalogId
                            )
                            .orElseThrow(() ->
                                    new InvalidInputException(
                                            "No existe un SharedAsset "
                                                    + "con stableCatalogId "
                                                    + stableCatalogId
                                    )
                            );

            result.put(
                    stableCatalogId,
                    asset
            );
        }

        return Map.copyOf(result);
    }

    private void addSharedCatalogId(
            Set<String> stableCatalogIds,
            InvestmentLedgerSnapshot.AssetReferenceSnapshot reference
    ) {
        if (reference == null
                || reference.kind()
                != AssetReferenceKind.SHARED) {

            return;
        }

        stableCatalogIds.add(
                reference.sharedAssetStableCatalogId()
        );
    }

    private AssetReference restoreAssetReference(
            InvestmentLedgerSnapshot.AssetReferenceSnapshot reference,
            Map<UUID, UUID> userAssetIdMap,
            Map<String, SharedAsset> sharedAssetsByStableId
    ) {
        if (reference == null) {
            return null;
        }

        if (reference.kind() == AssetReferenceKind.USER) {

            UUID restoredUserAssetId =
                    userAssetIdMap.get(
                            reference.userAssetId()
                    );

            if (restoredUserAssetId == null) {
                throw new InvalidInputException(
                        "La referencia USER apunta a un "
                                + "UserAsset inexistente en el snapshot."
                );
            }

            return AssetReference.user(
                    restoredUserAssetId
            );
        }

        SharedAsset sharedAsset =
                sharedAssetsByStableId.get(
                        reference.sharedAssetStableCatalogId()
                );

        if (sharedAsset == null) {
            throw new InvalidInputException(
                    "No se pudo resolver el SharedAsset "
                            + reference.sharedAssetStableCatalogId()
            );
        }

        return AssetReference.shared(
                sharedAsset.getId()
        );
    }

    private ActivityDetails restoreActivityDetails(
            InvestmentLedgerSnapshot.ActivitySnapshot snapshot,
            AssetReference assetReference,
            Map<UUID, CurrencyCode> userAssetCurrencies,
            Map<String, SharedAsset> sharedAssetsByStableId
    ) {
        return switch (snapshot.type()) {

            case BUY, SELL -> {

                if (!(snapshot.details()
                        instanceof InvestmentLedgerSnapshot
                        .TradeDetailsSnapshot trade)) {

                    throw invalidDetails(
                            snapshot.type()
                    );
                }

                Money unitPrice =
                        toMoney(
                                trade.unitPrice()
                        );

                Money settlementAmount =
                        toMoney(
                                trade.settlementAmount()
                        );

                AppliedFxRate appliedFxRate =
                        toAppliedFxRate(
                                trade.appliedFxRate()
                        );

                TradeSettlement settlement =
                        new TradeSettlement(
                                settlementAmount,
                                appliedFxRate
                        );

                yield new TradeActivityDetails(
                        trade.quantity(),
                        unitPrice,
                        settlement
                );
            }

            case DIVIDEND -> {

                if (snapshot.details()
                        instanceof InvestmentLedgerSnapshot
                        .CashDetailsSnapshot cash) {

                    yield new CashActivityDetails(
                            toMoney(
                                    cash.amount()
                            )
                    );
                }

                if (snapshot.details()
                        instanceof InvestmentLedgerSnapshot
                        .DividendUnitsDetailsSnapshot units) {

                    yield new DividendUnitsDetails(
                            units.quantity(),
                            toMoney(
                                    units.referenceUnitPrice()
                            )
                    );
                }

                throw invalidDetails(
                        snapshot.type()
                );
            }

            case DEPOSIT,
                    WITHDRAW,
                    OPENING_CASH -> {

                if (!(snapshot.details()
                        instanceof InvestmentLedgerSnapshot
                        .CashDetailsSnapshot cash)) {

                    throw invalidDetails(
                            snapshot.type()
                    );
                }

                yield new CashActivityDetails(
                        toMoney(
                                cash.amount()
                        )
                );
            }

            case EXCHANGE -> {

                if (!(snapshot.details()
                        instanceof InvestmentLedgerSnapshot
                        .ExchangeDetailsSnapshot exchange)) {

                    throw invalidDetails(
                            snapshot.type()
                    );
                }

                Money origin =
                        toMoney(
                                exchange.origin()
                        );

                Money destination =
                        toMoney(
                                exchange.destination()
                        );

                AppliedFxRate appliedFxRate =
                        toAppliedFxRate(
                                exchange.appliedFxRate()
                        );

                yield new ExchangeActivityDetails(
                        origin,
                        destination,
                        appliedFxRate
                );
            }

            case SPLIT -> {

                if (!(snapshot.details()
                        instanceof InvestmentLedgerSnapshot
                        .SplitDetailsSnapshot split)) {

                    throw invalidDetails(
                            snapshot.type()
                    );
                }

                yield new SplitActivityDetails(
                        split.ratio()
                );
            }

            case OPENING_POSITION -> {

                if (!(snapshot.details()
                        instanceof InvestmentLedgerSnapshot
                        .OpeningPositionDetailsSnapshot opening)) {

                    throw invalidDetails(
                            snapshot.type()
                    );
                }

                yield new OpeningPositionDetails(
                        opening.quantity(),
                        toMoney(
                                opening.acquisitionCost()
                        )
                );
            }
        };
    }

    private void validateRestoredActivitySemantics(
            Activity activity,
            UUID userId,
            Map<UUID, CurrencyCode> userAssetCurrencies,
            Map<String, SharedAsset> sharedAssetsByStableId
    ) {
        AssetReference reference =
                activity.getAssetReference();

        CurrencyCode assetCurrency =
                reference == null
                        ? null
                        : resolveAssetCurrency(
                                reference,
                                userAssetCurrencies,
                                sharedAssetsByStableId
                        );

        switch (activity.getType()) {

            case BUY, SELL -> {
                TradeActivityDetails trade =
                        (TradeActivityDetails)
                                activity.getDetails();

                if (!trade.unitPrice()
                        .currency()
                        .equals(assetCurrency)) {

                    throw new InvalidInputException(
                            activity.getType()
                                    + " tiene un unitPrice cuya moneda "
                                    + "no coincide con la moneda del Asset."
                    );
                }

                CurrencyCode localCurrency =
                        userCurrencies
                                .currencyAt(
                                        userId,
                                        activity.getOccurredAt()
                                )
                                .orElseThrow(() ->
                                        new InvalidInputException(
                                                "No existe moneda local para "
                                                        + "la fecha de la Activity."
                                        )
                                );

                validateTradeSettlement(
                        activity.getType(),
                        trade,
                        assetCurrency,
                        localCurrency
                );
            }

            case DEPOSIT, WITHDRAW -> {
                CashActivityDetails cash =
                        (CashActivityDetails)
                                activity.getDetails();

                CurrencyCode localCurrency =
                        userCurrencies
                                .currencyAt(
                                        userId,
                                        activity.getOccurredAt()
                                )
                                .orElseThrow(() ->
                                        new InvalidInputException(
                                                "No existe moneda local para "
                                                        + "la fecha de "
                                                        + activity.getType()
                                        )
                                );

                if (!cash.amount()
                        .currency()
                        .equals(localCurrency)) {

                    throw new InvalidInputException(
                            activity.getType()
                                    + " debe utilizar la moneda local "
                                    + "vigente en occurredAt."
                    );
                }
            }

            case DIVIDEND -> {
                if (activity.isUnitsDividend()) {

                    DividendUnitsDetails details =
                            (DividendUnitsDetails)
                                    activity.getDetails();

                    if (!details.referenceUnitPrice()
                            .currency()
                            .equals(assetCurrency)) {

                        throw new InvalidInputException(
                                "El referenceUnitPrice del "
                                        + "DIVIDEND en unidades debe "
                                        + "utilizar la moneda del Asset."
                        );
                    }
                }
            }

            case OPENING_POSITION -> {
                OpeningPositionDetails details =
                        (OpeningPositionDetails)
                                activity.getDetails();

                if (!details.acquisitionCost()
                        .currency()
                        .equals(assetCurrency)) {

                    throw new InvalidInputException(
                            "El acquisitionCost de "
                                    + "OPENING_POSITION debe utilizar "
                                    + "la moneda del Asset."
                    );
                }
            }

            default -> {
                // El propio dominio valida el resto de tipos.
            }
        }
    }

    private void validateTradeSettlement(
            ActivityType type,
            TradeActivityDetails trade,
            CurrencyCode assetCurrency,
            CurrencyCode localCurrency
    ) {
        CurrencyCode settlementCurrency =
                trade.settlement()
                        .amount()
                        .currency();

        AppliedFxRate fx =
                trade.settlement()
                        .appliedFxRate();

        if (settlementCurrency.equals(assetCurrency)) {

            if (fx != null) {
                throw new InvalidInputException(
                        "Un Trade liquidado en moneda del Asset "
                                + "no debe contener AppliedFxRate."
                );
            }

            return;
        }

        if (!settlementCurrency.equals(localCurrency)) {
            throw new InvalidInputException(
                    "La moneda de liquidación del Trade debe ser "
                            + "la moneda del Asset o la moneda local."
            );
        }

        if (assetCurrency.equals(localCurrency)) {

            if (fx != null) {
                throw new InvalidInputException(
                        "No debe existir AppliedFxRate cuando Asset "
                                + "y moneda local coinciden."
                );
            }

            return;
        }

        if (fx == null) {
            throw new InvalidInputException(
                    "Un Trade liquidado en moneda local para un "
                            + "Asset extranjero requiere AppliedFxRate."
            );
        }

        CurrencyCode expectedBase =
                type == ActivityType.BUY
                        ? localCurrency
                        : assetCurrency;

        CurrencyCode expectedQuote =
                type == ActivityType.BUY
                        ? assetCurrency
                        : localCurrency;

        if (!fx.baseCurrency()
                .equals(expectedBase)
                || !fx.quoteCurrency()
                .equals(expectedQuote)) {

            throw new InvalidInputException(
                    "El AppliedFxRate del Trade no corresponde "
                            + "a la dirección esperada."
            );
        }
    }

    private CurrencyCode resolveAssetCurrency(
            AssetReference reference,
            Map<UUID, CurrencyCode> userAssetCurrencies,
            Map<String, SharedAsset> sharedAssetsByStableId
    ) {
        if (reference.kind() == AssetReferenceKind.USER) {

            CurrencyCode currency =
                    userAssetCurrencies.get(
                            reference.id()
                    );

            if (currency == null) {
                throw new InvalidInputException(
                        "No se pudo resolver la moneda del UserAsset."
                );
            }

            return currency;
        }

        Optional<SharedAsset> asset =
                sharedAssetsByStableId
                        .values()
                        .stream()
                        .filter(candidate ->
                                candidate.getId()
                                        .equals(reference.id())
                        )
                        .findFirst();

        return asset
                .orElseThrow(() ->
                        new InvalidInputException(
                                "No se pudo resolver el SharedAsset."
                        )
                )
                .getCurrency();
    }

    private AssetSector restoreSector(
            InvestmentLedgerSnapshot.AssetSectorSnapshot snapshot
    ) {
        if (snapshot == null) {
            throw new InvalidInputException(
                    "El sector del UserAsset es obligatorio."
            );
        }

        if (snapshot.custom()) {
            return AssetSector.custom(
                    snapshot.displayName()
            );
        }

        String reference =
                snapshot.code();

        return sectors
                .findCommonByReference(reference)
                .orElseThrow(() ->
                        new InvalidInputException(
                                "No existe el sector común "
                                        + reference
                        )
                );
    }

    private com.puntomartinez.millete.investments.domain.model.AssetOrigin
    parseAssetOrigin(
            String value
    ) {
        try {
            return com.puntomartinez.millete.investments.domain.model.AssetOrigin
                    .valueOf(
                            value.trim().toUpperCase()
                    );
        } catch (RuntimeException exception) {
            throw new InvalidInputException(
                    "Origen de UserAsset no válido: "
                            + value
            );
        }
    }

    private Money toMoney(
            InvestmentLedgerSnapshot.MoneySnapshot snapshot
    ) {
        return new Money(
                snapshot.amount(),
                CurrencyCode.of(
                        snapshot.currency()
                )
        );
    }

    private AppliedFxRate toAppliedFxRate(
            InvestmentLedgerSnapshot.AppliedFxRateSnapshot snapshot
    ) {
        if (snapshot == null) {
            return null;
        }

        return new AppliedFxRate(
                CurrencyCode.of(
                        snapshot.baseCurrency()
                ),
                CurrencyCode.of(
                        snapshot.quoteCurrency()
                ),
                snapshot.rate(),
                snapshot.source(),
                snapshot.timestamp()
        );
    }

    private Map<Instant, Long> buildInitialOrderingState(
            List<Activity> existingActivities
    ) {
        Map<Instant, Long> result =
                new HashMap<>();

        for (Activity activity : existingActivities) {

            long current =
                    result.getOrDefault(
                            activity.getOccurredAt(),
                            -1L
                    );

            if (activity.getOrderingKey()
                    > current) {

                result.put(
                        activity.getOccurredAt(),
                        activity.getOrderingKey()
                );
            }
        }

        return result;
    }

    private long nextProvisionalOrderingKey(
            Map<Instant, Long> nextOrdering,
            Instant occurredAt
    ) {
        long next =
                nextOrdering.getOrDefault(
                        occurredAt,
                        -1L
                ) + 1L;

        nextOrdering.put(
                occurredAt,
                next
        );

        return next;
    }

    private void validatePortfolioIntegrityBeforePersist(
            UUID userId,
            List<Activity> existingActivities,
            List<Holding> existingHoldings,
            RestoreContext context,
            Instant trackingStartAt
    ) {
        List<Activity> combinedActivities =
                new ArrayList<>(
                        existingActivities
                );

        context.activities()
                .stream()
                .map(RestoredActivity::provisionalActivity)
                .forEach(combinedActivities::add);

        List<Holding> combinedHoldings =
                new ArrayList<>(
                        existingHoldings
                );

        combinedHoldings.addAll(
                context.holdings()
        );

        PortfolioReplay.replay(
                userId,
                combinedActivities,
                combinedHoldings,
                trackingStartAt
        );
    }

    private void persistUserAssets(
            List<UserAsset> assets
    ) {
        for (UserAsset asset : assets) {
            userAssets.save(asset);
        }
    }

    private void persistUserAssetPrices(
            List<UserAssetPrice> prices
    ) {
        for (UserAssetPrice price : prices) {
            userAssetPrices.save(price);
        }
    }

    private void persistHoldings(
            List<Holding> holdings
    ) {
        for (Holding holding : holdings) {
            this.holdings.save(holding);
        }
    }

    private void persistActivities(
            UUID userId,
            List<RestoredActivity> restoredActivities
    ) {
        for (RestoredActivity restored
                : restoredActivities) {

            InvestmentLedgerSnapshot.ActivitySnapshot snapshot =
                    restored.snapshot();

            Activity provisional =
                    restored.provisionalActivity();

            long orderingKey =
                    ordering.nextOrderingKey(
                            userId,
                            snapshot.occurredAt()
                    );

            Activity activity =
                    Activity.reconstitute(
                            provisional.getId(),
                            userId,
                            provisional.getType(),
                            provisional.getAssetReference(),
                            provisional.getOccurredAt(),
                            provisional.getCreatedAt(),
                            provisional.getModifiedAt(),
                            orderingKey,
                            provisional.getDetails(),
                            provisional.getComment(),
                            null
                    );

            activities.save(activity);

            if (activity.getType()
                    == ActivityType.DEPOSIT
                    || activity.getType()
                    == ActivityType.WITHDRAW) {

                CashActivityDetails cash =
                        (CashActivityDetails)
                                activity.getDetails();

                TransactionIntegrationPort.TransferDirection
                        direction =
                        activity.getType()
                                == ActivityType.DEPOSIT
                                ? TransactionIntegrationPort
                                .TransferDirection.TRANSFER_OUT
                                : TransactionIntegrationPort
                                .TransferDirection.TRANSFER_IN;

                UUID transactionId =
                        transactions.createInvestmentTransfer(
                                userId,
                                activity.getId(),
                                direction,
                                cash.amount(),
                                activity.getOccurredAt(),
                                activity.getComment()
                        );

                activity.attachTransaction(
                        transactionId
                );

                activities.save(activity);
            }
        }
    }

    private void persistAudits(
            UUID userId,
            List<InvestmentLedgerSnapshot.ActivityAuditSnapshot>
                    auditSnapshots,
            Map<UUID, UUID> activityIdMap
    ) {
        for (InvestmentLedgerSnapshot.ActivityAuditSnapshot
                snapshot : auditSnapshots) {

            UUID restoredActivityId =
                    activityIdMap.get(
                            snapshot.activityId()
                    );

            if (restoredActivityId == null) {
                throw new InvalidInputException(
                        "El audit referencia una Activity "
                                + "que no existe en el snapshot."
                );
            }

            ActivityAudit audit =
                    new ActivityAudit(
                            UUID.randomUUID(),
                            restoredActivityId,
                            userId,
                            snapshot.beforeJson(),
                            snapshot.afterJson(),
                            snapshot.reason(),
                            snapshot.changedAt()
                    );

            audits.save(audit);
        }
    }

    private void validateActivityAuditReferences(
            InvestmentLedgerSnapshot snapshot,
            Map<UUID, UUID> activityIdMap
    ) {
        for (InvestmentLedgerSnapshot.ActivityAuditSnapshot audit
                : snapshot.audit()) {

            if (!activityIdMap.containsKey(
                    audit.activityId()
            )) {
                throw new InvalidInputException(
                        "El audit "
                                + audit.id()
                                + " referencia una Activity "
                                + "que no existe en el snapshot."
                );
            }
        }
    }

    private void validateSnapshot(
            InvestmentLedgerSnapshot snapshot
    ) {
        if (snapshot.version()
                != InvestmentLedgerSnapshot.CURRENT_VERSION) {

            throw new InvalidInputException(
                    "Versión de InvestmentLedgerSnapshot no soportada: "
                            + snapshot.version()
            );
        }
    }

    private void validateUserId(
            UUID userId
    ) {
        if (userId == null) {
            throw new InvalidInputException(
                    "El usuario es obligatorio."
            );
        }
    }

    private void validateUniqueUserAssetIds(
            List<InvestmentLedgerSnapshot.UserAssetSnapshot>
                    snapshots
    ) {
        Set<UUID> ids =
                new HashSet<>();

        for (InvestmentLedgerSnapshot.UserAssetSnapshot snapshot
                : snapshots) {

            if (!ids.add(snapshot.id())) {
                throw new InvalidInputException(
                        "UserAsset duplicado en el snapshot: "
                                + snapshot.id()
                );
            }
        }
    }

    private void validateUniqueUserAssetPriceIds(
            List<InvestmentLedgerSnapshot.UserAssetPriceSnapshot>
                    snapshots
    ) {
        Set<UUID> ids =
                new HashSet<>();

        for (InvestmentLedgerSnapshot.UserAssetPriceSnapshot snapshot
                : snapshots) {

            if (!ids.add(snapshot.id())) {
                throw new InvalidInputException(
                        "UserAssetPrice duplicado en el snapshot: "
                                + snapshot.id()
                );
            }
        }
    }

    private void validateUniqueHoldingIds(
            List<InvestmentLedgerSnapshot.HoldingSnapshot>
                    snapshots
    ) {
        Set<UUID> ids =
                new HashSet<>();

        for (InvestmentLedgerSnapshot.HoldingSnapshot snapshot
                : snapshots) {

            if (!ids.add(snapshot.id())) {
                throw new InvalidInputException(
                        "Holding duplicado en el snapshot: "
                                + snapshot.id()
                );
            }
        }
    }

    private void validateUniqueActivityIds(
            List<InvestmentLedgerSnapshot.ActivitySnapshot>
                    snapshots
    ) {
        Set<UUID> ids =
                new HashSet<>();

        for (InvestmentLedgerSnapshot.ActivitySnapshot snapshot
                : snapshots) {

            if (!ids.add(snapshot.id())) {
                throw new InvalidInputException(
                        "Activity duplicada en el snapshot: "
                                + snapshot.id()
                );
            }
        }
    }

    private void validateUniqueActivitySequences(
            List<InvestmentLedgerSnapshot.ActivitySnapshot>
                    snapshots
    ) {
        Set<Long> sequences =
                new HashSet<>();

        for (InvestmentLedgerSnapshot.ActivitySnapshot snapshot
                : snapshots) {

            if (!sequences.add(snapshot.sequence())) {
                throw new InvalidInputException(
                        "Sequence duplicado en el snapshot de Activities: "
                                + snapshot.sequence()
                );
            }
        }
    }

    private void validateUniqueAuditIds(
            List<InvestmentLedgerSnapshot.ActivityAuditSnapshot>
                    snapshots
    ) {
        Set<UUID> ids =
                new HashSet<>();

        for (InvestmentLedgerSnapshot.ActivityAuditSnapshot snapshot
                : snapshots) {

            if (!ids.add(snapshot.id())) {
                throw new InvalidInputException(
                        "Audit duplicado en el snapshot: "
                                + snapshot.id()
                );
            }
        }
    }

    private IllegalArgumentException invalidDetails(
            ActivityType type
    ) {
        return new InvalidInputException(
                "Los detalles del snapshot no son compatibles "
                        + "con ActivityType "
                        + type
        );
    }

    private record RestoredActivity(
            InvestmentLedgerSnapshot.ActivitySnapshot snapshot,
            Activity provisionalActivity
    ) {
    }

    private record RestoreContext(
            List<UserAsset> userAssets,
            List<UserAssetPrice> userAssetPrices,
            List<Holding> holdings,
            List<RestoredActivity> activities,
            Map<UUID, UUID> userAssetIdMap,
            Map<UUID, UUID> activityIdMap
    ) {
    }
}