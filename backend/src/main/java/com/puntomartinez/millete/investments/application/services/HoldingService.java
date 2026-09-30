package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityDetails;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AppliedFxRate;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.HoldingStatus;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.PortfolioReplay;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeSettlement;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.CreateHoldingUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetActiveHoldingUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListHoldingsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ReplaceHoldingWithHistoryUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityOrderingPort;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.HoldingRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentTrackingSettingsRepository;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class HoldingService implements
        CreateHoldingUseCase,
        GetActiveHoldingUseCase,
        ListHoldingsUseCase,
        ReplaceHoldingWithHistoryUseCase {

    private static final int AMOUNT_SCALE = 12;

    private final HoldingRepository holdings;
    private final ActivityRepository activities;
    private final ActivityOrderingPort ordering;
    private final InvestmentPortfolioLockPort portfolioLock;
    private final InvestmentTrackingSettingsRepository trackingSettings;
    private final SharedAssetRepository sharedAssets;
    private final UserAssetRepository userAssets;
    private final AssetPriceRepository assetPrices;
    private final UserAssetPriceRepository userAssetPrices;
    private final UserCurrencyPort userCurrencies;
    private final FxRateRepository fxRates;
    private final PortfolioRebuildService portfolioRebuild;
    private final TimeProvider time;

    public HoldingService(
            HoldingRepository holdings,
            ActivityRepository activities,
            ActivityOrderingPort ordering,
            InvestmentPortfolioLockPort portfolioLock,
            InvestmentTrackingSettingsRepository trackingSettings,
            SharedAssetRepository sharedAssets,
            UserAssetRepository userAssets,
            AssetPriceRepository assetPrices,
            UserAssetPriceRepository userAssetPrices,
            UserCurrencyPort userCurrencies,
            FxRateRepository fxRates,
            PortfolioRebuildService portfolioRebuild,
            TimeProvider time
    ) {
        this.holdings = holdings;
        this.activities = activities;
        this.ordering = ordering;
        this.portfolioLock = portfolioLock;
        this.trackingSettings = trackingSettings;
        this.sharedAssets = sharedAssets;
        this.userAssets = userAssets;
        this.assetPrices = assetPrices;
        this.userAssetPrices = userAssetPrices;
        this.userCurrencies = userCurrencies;
        this.fxRates = fxRates;
        this.portfolioRebuild = portfolioRebuild;
        this.time = time;
    }

    @Override
    @Transactional
    public Holding create(
            UUID userId,
            CreateHoldingCommand command
    ) {
        portfolioLock.lock(userId);

        Objects.requireNonNull(command, "command");

        AssetCurrency asset =
                resolveAsset(
                        userId,
                        command.assetReference()
                );

        ensureNoActiveHolding(
                userId,
                command.assetReference()
        );

        ensureNoActivityAtOrBeforeSnapshot(
                userId,
                command.assetReference(),
                command.snapshotAt()
        );

        Money acquisitionCost =
                new Money(
                        command.acquisitionCost(),
                        asset.currency()
                );

        Holding holding =
                Holding.create(
                        time,
                        userId,
                        command.assetReference(),
                        command.snapshotAt(),
                        command.quantity(),
                        acquisitionCost
                );

        Holding saved =
                holdings.save(holding);

        portfolioRebuild.rebuild(userId);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Holding get(
            UUID userId,
            AssetReference assetReference
    ) {
        return holdings.findAllByUserId(userId)
                .stream()
                .filter(holding ->
                        holding.getStatus() == HoldingStatus.ACTIVE
                                && holding.getAssetReference()
                                .equals(assetReference)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active Holding no encontrado"
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Holding> list(
            UUID userId,
            AssetReference assetReference
    ) {
        List<Holding> result =
                holdings.findAllByUserId(userId);

        if (assetReference == null) {
            return List.copyOf(result);
        }

        return result.stream()
                .filter(holding ->
                        holding.getAssetReference()
                                .equals(assetReference)
                )
                .toList();
    }

    @Override
    @Transactional
    public List<Activity> replace(
            UUID userId,
            UUID holdingId,
            ReplaceHoldingWithHistoryCommand command
    ) {
        portfolioLock.lock(userId);

        Holding holding =
                holdings.findByIdAndUserId(
                        holdingId,
                        userId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Holding no encontrado"
                        )
                );

        if (holding.getStatus()
                != HoldingStatus.ACTIVE) {

            throw new InvalidInputException(
                    "Solo un Holding ACTIVE puede ser reemplazado por historial."
            );
        }

        if (command == null
                || command.history() == null
                || command.history().isEmpty()) {

            throw new InvalidInputException(
                    "El historial de reemplazo no puede estar vacío."
            );
        }

        AssetCurrency asset =
                resolveAsset(
                        userId,
                        holding.getAssetReference()
                );

        List<Activity> historicalActivities =
                buildHistoricalActivities(
                        userId,
                        holding,
                        asset,
                        command.history()
                );

        validateReplacementHistory(
                userId,
                holding,
                historicalActivities
        );

        for (Activity activity : historicalActivities) {
            activities.save(activity);
        }

        holding.markSuperseded(time);
        holdings.save(holding);

        portfolioRebuild.rebuild(userId);

        return List.copyOf(historicalActivities);
    }

    private List<Activity> buildHistoricalActivities(
            UUID userId,
            Holding holding,
            AssetCurrency asset,
            List<HistoricalActivity> history
    ) {
        List<Activity> result =
                new ArrayList<>();

        for (HistoricalActivity historical : history) {

            if (historical == null) {
                throw new InvalidInputException(
                        "El historial contiene una entrada nula."
                );
            }

            if (historical.occurredAt() == null) {
                throw new InvalidInputException(
                        "Cada Activity histórica requiere occurredAt."
                );
            }

            if (historical.occurredAt()
                    .isAfter(holding.getSnapshotAt())) {

                throw new InvalidInputException(
                        "Las Activities históricas deben ocurrir "
                                + "en o antes del snapshot del Holding."
                );
            }

            ActivityDetailsResult details =
                    buildHistoricalDetails(
                            userId,
                            holding.getAssetReference(),
                            asset,
                            historical
                    );

            long orderingKey =
                    ordering.nextOrderingKey(
                            userId,
                            historical.occurredAt()
                    );

            Activity activity =
                    Activity.create(
                            time,
                            userId,
                            details.type(),
                            holding.getAssetReference(),
                            historical.occurredAt(),
                            orderingKey,
                            details.details(),
                            details.comment()
                    );

            result.add(activity);
        }

        return List.copyOf(result);
    }

    private ActivityDetailsResult buildHistoricalDetails(
            UUID userId,
            AssetReference assetReference,
            AssetCurrency asset,
            HistoricalActivity historical
    ) {
        return switch (historical.details()) {

            case BuyDetails details ->
                    buildHistoricalTrade(
                            userId,
                            assetReference,
                            asset.currency(),
                            historical.occurredAt(),
                            ActivityType.BUY,
                            details.quantity(),
                            details.unitPrice(),
                            details.settlementCurrency(),
                            details.comment()
                    );

            case SellDetails details ->
                    buildHistoricalTrade(
                            userId,
                            assetReference,
                            asset.currency(),
                            historical.occurredAt(),
                            ActivityType.SELL,
                            details.quantity(),
                            details.unitPrice(),
                            details.settlementCurrency(),
                            details.comment()
                    );

            case CashDividendDetails details -> {
                Money amount =
                        new Money(
                                details.amount(),
                                CurrencyCode.of(
                                        details.currency()
                                )
                        );

                yield new ActivityDetailsResult(
                        ActivityType.DIVIDEND,
                        new CashActivityDetails(amount),
                        details.comment()
                );
            }

            case InKindDividendDetails details -> {
                Money referenceUnitPrice =
                        findReferenceUnitPrice(
                                userId,
                                assetReference,
                                asset,
                                historical.occurredAt()
                        );

                yield new ActivityDetailsResult(
                        ActivityType.DIVIDEND,
                        new DividendUnitsDetails(
                                details.quantity(),
                                referenceUnitPrice
                        ),
                        details.comment()
                );
            }

            case SplitDetails details ->
                    new ActivityDetailsResult(
                            ActivityType.SPLIT,
                            new SplitActivityDetails(
                                    details.ratio()
                            ),
                            details.comment()
                    );
        };
    }

    private ActivityDetailsResult buildHistoricalTrade(
            UUID userId,
            AssetReference assetReference,
            CurrencyCode assetCurrency,
            Instant occurredAt,
            ActivityType type,
            BigDecimal quantity,
            BigDecimal inputUnitPrice,
            com.puntomartinez.millete.investments.domain.model.SettlementCurrency settlementCurrency,
            String comment
    ) {
        if (settlementCurrency == null) {
            throw new InvalidInputException(
                    "La moneda de liquidación es obligatoria."
            );
        }

        if (quantity == null
                || quantity.signum() <= 0) {

            throw new InvalidInputException(
                    "La cantidad debe ser positiva."
            );
        }

        if (inputUnitPrice == null
                || inputUnitPrice.signum() <= 0) {

            throw new InvalidInputException(
                    "El precio unitario debe ser positivo."
            );
        }

        CurrencyCode localCurrency =
                userCurrencies
                        .currencyAt(
                                userId,
                                occurredAt
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe moneda local para el instante histórico."
                                )
                        );

        CurrencyCode settlementCurrencyCode =
                settlementCurrency
                        == com.puntomartinez.millete.investments.domain.model.SettlementCurrency.LOCAL
                        ? localCurrency
                        : assetCurrency;

        Money inputUnitPriceMoney =
                new Money(
                        inputUnitPrice,
                        settlementCurrencyCode
                );

        Money settlementAmount =
                inputUnitPriceMoney.multiply(
                        quantity,
                        AMOUNT_SCALE
                );

        AppliedFxRate appliedFxRate =
                null;

        Money assetUnitPrice;

        if (settlementCurrency
                == com.puntomartinez.millete.investments.domain.model.SettlementCurrency.ASSET
                || assetCurrency.equals(localCurrency)) {

            assetUnitPrice =
                    new Money(
                            inputUnitPrice,
                            assetCurrency
                    );

        } else {

            CurrencyCode baseCurrency =
                    type == ActivityType.BUY
                            ? localCurrency
                            : assetCurrency;

            CurrencyCode quoteCurrency =
                    type == ActivityType.BUY
                            ? assetCurrency
                            : localCurrency;

            FxRate fxRate =
                    findFxRate(
                            baseCurrency,
                            quoteCurrency,
                            occurredAt
                    );

            appliedFxRate =
                    new AppliedFxRate(
                            baseCurrency,
                            quoteCurrency,
                            fxRate.rate(),
                            fxRate.source(),
                            fxRate.timestamp()
                    );

            assetUnitPrice =
                    type == ActivityType.BUY
                            ? inputUnitPriceMoney.multiply(
                                    fxRate.rate(),
                                    AMOUNT_SCALE
                            )
                            : inputUnitPriceMoney.divide(
                                    fxRate.rate(),
                                    AMOUNT_SCALE
                            );
        }

        TradeSettlement settlement =
                new TradeSettlement(
                        settlementAmount,
                        appliedFxRate
                );

        return new ActivityDetailsResult(
                type,
                new TradeActivityDetails(
                        quantity,
                        assetUnitPrice,
                        settlement
                ),
                comment
        );
    }

    private Money findReferenceUnitPrice(
            UUID userId,
            AssetReference reference,
            AssetCurrency asset
    ) {
        return findReferenceUnitPrice(
                userId,
                reference,
                asset,
                time.now()
        );
    }

    private Money findReferenceUnitPrice(
            UUID userId,
            AssetReference reference,
            AssetCurrency asset,
            Instant occurredAt
    ) {
        return switch (reference.kind()) {

            case SHARED -> {
                AssetPrice price =
                        assetPrices.findLatestAt(
                                        reference.id(),
                                        occurredAt
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "No existe un precio de mercado disponible para el dividendo."
                                        )
                                );

                BigDecimal valuationPrice =
                        price.valuationPrice();

                if (valuationPrice == null) {
                    throw new IllegalStateException(
                            "El precio de mercado no contiene un valor de valoración."
                    );
                }

                yield new Money(
                        valuationPrice,
                        asset.currency()
                );
            }

            case USER -> {
                UserAssetPrice price =
                        userAssetPrices.findLatestAt(
                                        reference.id(),
                                        occurredAt
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "No existe una valoración disponible para el dividendo."
                                        )
                                );

                yield price.getUnitPrice();
            }
        };
    }

    private void validateReplacementHistory(
            UUID userId,
            Holding holding,
            List<Activity> historicalActivities
    ) {
        List<Activity> allActivities =
                new ArrayList<>(
                        activities.findAllByUserId(userId)
                );

        allActivities.addAll(
                historicalActivities
        );

        List<Activity> throughSnapshot =
                allActivities.stream()
                        .filter(activity ->
                                !activity.getOccurredAt()
                                        .isAfter(
                                                holding.getSnapshotAt()
                                        ))
                        .sorted(
                                java.util.Comparator
                                        .comparing(
                                                Activity::getOccurredAt
                                        )
                                        .thenComparingLong(
                                                Activity::getOrderingKey
                                        )
                                        .thenComparing(
                                                activity ->
                                                        activity
                                                                .getId()
                                                                .toString()
                                        )
                        )
                        .toList();

        List<Holding> otherHoldings =
                holdings.findAllByUserId(userId)
                        .stream()
                        .filter(candidate ->
                                !candidate.getId()
                                        .equals(
                                                holding.getId()
                                        )
                        )
                        .toList();

        Instant trackingStartAt =
                trackingSettings
                        .findTrackingStartAt(userId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El tracking de Investments no está configurado."
                                )
                        );

        PortfolioReplay.Result replay =
                PortfolioReplay.replay(
                        userId,
                        throughSnapshot,
                        otherHoldings,
                        trackingStartAt
                );

        var position =
                replay.positions()
                        .stream()
                        .filter(candidate ->
                                candidate
                                        .assetReference()
                                        .equals(
                                                holding
                                                        .getAssetReference()
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new InvalidInputException(
                                        "El historial no reconstruye ninguna posición para el Holding."
                                )
                        );

        if (position.quantity()
                .compareTo(
                        holding.getQuantity()
                ) != 0) {

            throw new InvalidInputException(
                    "El historial no coincide con la cantidad del Holding."
            );
        }

        if (!position.acquisitionCost()
                .currency()
                .equals(
                        holding.getAcquisitionCost()
                                .currency()
                )) {

            throw new InvalidInputException(
                    "La moneda del coste reconstruido no coincide con el Holding."
            );
        }

        if (position.acquisitionCost()
                .amount()
                .compareTo(
                        holding.getAcquisitionCost()
                                .amount()
                ) != 0) {

            throw new InvalidInputException(
                    "El historial no coincide con el coste de adquisición del Holding."
            );
        }
    }

    private void ensureNoActiveHolding(
            UUID userId,
            AssetReference assetReference
    ) {
        boolean exists =
                holdings.findAllByUserId(userId)
                        .stream()
                        .anyMatch(holding ->
                                holding.getStatus()
                                        == HoldingStatus.ACTIVE
                                        && holding
                                        .getAssetReference()
                                        .equals(assetReference)
                        );

        if (exists) {
            throw new InvalidInputException(
                    "Ya existe un Holding ACTIVE para este Asset."
            );
        }
    }

    private void ensureNoActivityAtOrBeforeSnapshot(
            UUID userId,
            AssetReference assetReference,
            Instant snapshotAt
    ) {
        boolean conflict =
                activities.findAllByUserId(userId)
                        .stream()
                        .anyMatch(activity ->
                                assetReference.equals(
                                        activity.getAssetReference()
                                )
                                        && !activity.getOccurredAt()
                                        .isAfter(snapshotAt)
                        );

        if (conflict) {
            throw new InvalidInputException(
                    "No puede crearse un Holding ACTIVE si existen Activities "
                            + "del mismo Asset en o antes de snapshotAt. "
                            + "Utiliza ReplaceHoldingWithHistory."
            );
        }
    }

    private AssetCurrency resolveAsset(
            UUID userId,
            AssetReference reference
    ) {
        if (reference == null) {
            throw new InvalidInputException(
                    "El AssetReference es obligatorio."
            );
        }

        return switch (reference.kind()) {

            case SHARED -> {
                SharedAsset asset =
                        sharedAssets.findById(
                                        reference.id()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "SharedAsset no encontrado"
                                        )
                                );

                yield new AssetCurrency(
                        asset.getCurrency()
                );
            }

            case USER -> {
                UserAsset asset =
                        userAssets.findByIdAndUserId(
                                        reference.id(),
                                        userId
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "UserAsset no encontrado"
                                        )
                                );

                yield new AssetCurrency(
                        asset.getCurrency()
                );
            }
        };
    }

    private FxRate findFxRate(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency,
            Instant at
    ) {
        return fxRates
                .findLatestAt(
                        baseCurrency,
                        quoteCurrency,
                        at
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe un tipo de cambio histórico disponible entre "
                                        + baseCurrency.value()
                                        + " y "
                                        + quoteCurrency.value()
                        )
                );
    }

    private record AssetCurrency(
            CurrencyCode currency
    ) {
    }

    private record ActivityDetailsResult(
            ActivityType type,
            ActivityDetails details,
            String comment
    ) {
    }
}