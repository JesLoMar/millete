package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.Lot;
import com.puntomartinez.millete.investments.domain.model.LotSourceType;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.OpeningPositionDetails;
import com.puntomartinez.millete.investments.domain.model.PerformanceAttribution;
import com.puntomartinez.millete.investments.domain.model.PortfolioReplay;
import com.puntomartinez.millete.investments.domain.model.Position;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.CalculatePerformanceUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.HoldingRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentTrackingSettingsRepository;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PerformanceService implements CalculatePerformanceUseCase {

    private static final int MONEY_SCALE = 12;
    private static final int PRICE_SCALE = 18;

    private final ActivityRepository activities;
    private final HoldingRepository holdings;
    private final InvestmentTrackingSettingsRepository trackingSettings;
    private final SharedAssetRepository sharedAssets;
    private final UserAssetRepository userAssets;
    private final AssetPriceRepository assetPrices;
    private final UserAssetPriceRepository userAssetPrices;
    private final FxRateRepository fxRates;
    private final UserCurrencyPort userCurrencies;

    public PerformanceService(
            ActivityRepository activities,
            HoldingRepository holdings,
            InvestmentTrackingSettingsRepository trackingSettings,
            SharedAssetRepository sharedAssets,
            UserAssetRepository userAssets,
            AssetPriceRepository assetPrices,
            UserAssetPriceRepository userAssetPrices,
            FxRateRepository fxRates,
            UserCurrencyPort userCurrencies
    ) {
        this.activities = activities;
        this.holdings = holdings;
        this.trackingSettings = trackingSettings;
        this.sharedAssets = sharedAssets;
        this.userAssets = userAssets;
        this.assetPrices = assetPrices;
        this.userAssetPrices = userAssetPrices;
        this.fxRates = fxRates;
        this.userCurrencies = userCurrencies;
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceAttribution calculate(
            UUID userId,
            Instant from,
            Instant to
    ) {
        validateUserId(userId);
        validatePeriod(from, to);

        CurrencyCode reportingCurrency =
                userCurrencies
                        .currencyAt(userId, to)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe moneda local para el final del período."
                                )
                        );

        Instant trackingStartAt =
                trackingSettings
                        .findTrackingStartAt(userId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El tracking de Investments no está configurado."
                                )
                        );

        List<Activity> allActivities =
                activities.findAllByUserId(userId)
                        .stream()
                        .filter(activity ->
                                !activity.getOccurredAt().isAfter(to)
                        )
                        .sorted(activityOrder())
                        .toList();

        List<Holding> allHoldings =
                holdings.findAllByUserId(userId)
                        .stream()
                        .filter(holding ->
                                !holding.getSnapshotAt().isAfter(to)
                        )
                        .toList();

        PortfolioReplay.Result openingReplay =
                replayAt(
                        userId,
                        from,
                        allActivities,
                        allHoldings,
                        trackingStartAt
                );

        PortfolioReplay.Result endingReplay =
                replayAt(
                        userId,
                        to,
                        allActivities,
                        allHoldings,
                        trackingStartAt
                );

        CalculationAccumulator calculation =
                new CalculationAccumulator();

        BigDecimal openingValue =
                valuePortfolio(
                        openingReplay,
                        userId,
                        from,
                        reportingCurrency,
                        calculation
                );

        BigDecimal endingValue =
                valuePortfolio(
                        endingReplay,
                        userId,
                        to,
                        reportingCurrency,
                        calculation
                );

        calculateFlows(
                allActivities,
                from,
                to,
                reportingCurrency,
                calculation
        );

        Map<UUID, Activity> activitiesById =
                new LinkedHashMap<>();

        for (Activity activity : allActivities) {
            activitiesById.put(
                    activity.getId(),
                    activity
            );
        }

        Map<UUID, Lot> lotsAtFrom =
                new HashMap<>();

        for (Lot lot : openingReplay.lots()) {
            lotsAtFrom.put(
                    lot.getId(),
                    lot
            );
        }

        Map<UUID, Lot> lotsAtEnd =
                new HashMap<>();

        for (Lot lot : endingReplay.lots()) {
            lotsAtEnd.put(
                    lot.getId(),
                    lot
            );
        }

        calculateRealizedGains(
                userId,
                from,
                to,
                reportingCurrency,
                endingReplay,
                lotsAtFrom,
                lotsAtEnd,
                activitiesById,
                allActivities,
                calculation
        );

        calculateOpenPositionEffects(
                userId,
                from,
                to,
                reportingCurrency,
                endingReplay,
                lotsAtFrom,
                activitiesById,
                allActivities,
                calculation
        );

        calculateCashFxEffect(
                userId,
                from,
                to,
                reportingCurrency,
                openingReplay.cashBalances(),
                allActivities,
                calculation
        );

        if (calculation.hasMissingData()) {
            return PerformanceAttribution.notCalculable(
                    from,
                    to,
                    reportingCurrency,
                    money(
                            openingValue,
                            reportingCurrency
                    ),
                    money(
                            endingValue,
                            reportingCurrency
                    ),
                    calculation.unavailableReason()
            );
        }

        BigDecimal portfolioChange =
                endingValue.subtract(openingValue);

        BigDecimal reconciliationDifference =
                portfolioChange
                        .subtract(calculation.openingCapital)
                        .subtract(calculation.contributions)
                        .add(calculation.withdrawals)
                        .subtract(calculation.realizedGains)
                        .subtract(calculation.unrealizedPriceEffect)
                        .subtract(calculation.fxEffect)
                        .subtract(calculation.cashDividends)
                        .subtract(calculation.inKindDividends)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

        return PerformanceAttribution.calculable(
                from,
                to,
                reportingCurrency,
                money(
                        openingValue,
                        reportingCurrency
                ),
                money(
                        endingValue,
                        reportingCurrency
                ),
                money(
                        calculation.openingCapital,
                        reportingCurrency
                ),
                money(
                        calculation.contributions,
                        reportingCurrency
                ),
                money(
                        calculation.withdrawals,
                        reportingCurrency
                ),
                money(
                        calculation.realizedGains,
                        reportingCurrency
                ),
                money(
                        calculation.unrealizedPriceEffect,
                        reportingCurrency
                ),
                money(
                        calculation.fxEffect,
                        reportingCurrency
                ),
                money(
                        calculation.cashDividends,
                        reportingCurrency
                ),
                money(
                        calculation.inKindDividends,
                        reportingCurrency
                ),
                money(
                        reconciliationDifference,
                        reportingCurrency
                ),
                calculation.estimated
        );
    }

    private PortfolioReplay.Result replayAt(
            UUID userId,
            Instant asOf,
            List<Activity> allActivities,
            List<Holding> allHoldings,
            Instant trackingStartAt
    ) {
        List<Activity> activitiesAt =
                allActivities.stream()
                        .filter(activity ->
                                !activity.getOccurredAt().isAfter(asOf)
                        )
                        .toList();

        List<Holding> holdingsAt =
                allHoldings.stream()
                        .filter(holding ->
                                !holding.getSnapshotAt().isAfter(asOf)
                        )
                        .toList();

        return PortfolioReplay.replay(
                userId,
                activitiesAt,
                holdingsAt,
                trackingStartAt
        );
    }

    private BigDecimal valuePortfolio(
            PortfolioReplay.Result replay,
            UUID userId,
            Instant asOf,
            CurrencyCode reportingCurrency,
            CalculationAccumulator calculation
    ) {
        BigDecimal total =
                BigDecimal.ZERO;

        for (Money balance : replay.cashBalances().values()) {
            if (balance.amount().signum() == 0) {
                continue;
            }

            Optional<ConvertedAmount> converted =
                    convertToReporting(
                            balance,
                            reportingCurrency,
                            asOf
                    );

            if (converted.isEmpty()) {
                calculation.missing(
                        "CASH_FX:"
                                + balance.currency().value()
                                + ":"
                                + asOf
                );
                continue;
            }

            total =
                    total.add(
                            converted.get().amount()
                    );

            calculation.estimated |=
                    converted.get().estimated();
        }

        for (Position position : replay.positions()) {
            PricePoint price =
                    priceAt(
                            userId,
                            position.assetReference(),
                            asOf
                    );

            if (price == null) {
                calculation.missing(
                        "POSITION_PRICE:"
                                + position.assetReference().kind()
                                + ":"
                                + position.assetReference().id()
                );
                continue;
            }

            FxQuote fx =
                    fxQuote(
                            price.currency(),
                            reportingCurrency,
                            asOf
                    );

            if (fx == null) {
                calculation.missing(
                        "POSITION_FX:"
                                + price.currency().value()
                                + ":"
                                + reportingCurrency.value()
                                + ":"
                                + asOf
                );
                continue;
            }

            BigDecimal nativeValue =
                    price.unitPrice()
                            .multiply(
                                    position.quantity()
                            );

            BigDecimal localValue =
                    nativeValue
                            .multiply(
                                    fx.rate()
                            )
                            .setScale(
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );

            total =
                    total.add(localValue);

            calculation.estimated |=
                    price.estimated();

            calculation.estimated |=
                    fx.estimated();

            calculation.estimated |=
                    position.estimated();
        }

        return total.setScale(
                MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private void calculateFlows(
            List<Activity> allActivities,
            Instant from,
            Instant to,
            CurrencyCode reportingCurrency,
            CalculationAccumulator calculation
    ) {
        for (Activity activity : allActivities) {
            Instant occurredAt =
                    activity.getOccurredAt();

            if (!occurredAt.isAfter(from)
                    || occurredAt.isAfter(to)) {
                continue;
            }

            switch (activity.getType()) {
                case DEPOSIT -> {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    addConvertedAmount(
                            details.amount(),
                            occurredAt,
                            reportingCurrency,
                            calculation,
                            FlowType.CONTRIBUTION,
                            activity
                    );
                }

                case WITHDRAW -> {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    addConvertedAmount(
                            details.amount(),
                            occurredAt,
                            reportingCurrency,
                            calculation,
                            FlowType.WITHDRAWAL,
                            activity
                    );
                }

                case OPENING_CASH -> {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    addConvertedAmount(
                            details.amount(),
                            occurredAt,
                            reportingCurrency,
                            calculation,
                            FlowType.OPENING_CAPITAL,
                            activity
                    );
                }

                case DIVIDEND -> {
                    if (activity.getDetails()
                            instanceof CashActivityDetails details) {

                        addConvertedAmount(
                                details.amount(),
                                occurredAt,
                                reportingCurrency,
                                calculation,
                                FlowType.CASH_DIVIDEND,
                                activity
                        );

                    } else if (activity.getDetails()
                            instanceof DividendUnitsDetails details) {

                        Money referenceValue =
                                details.referenceUnitPrice()
                                        .multiply(
                                                details.quantity(),
                                                MONEY_SCALE
                                        );

                        addConvertedAmount(
                                referenceValue,
                                occurredAt,
                                reportingCurrency,
                                calculation,
                                FlowType.IN_KIND_DIVIDEND,
                                activity
                        );
                    }
                }

                default -> {
                    // BUY, SELL, SPLIT, EXCHANGE and
                    // OPENING_POSITION are handled elsewhere.
                }
            }
        }
    }

    private void calculateRealizedGains(
            UUID userId,
            Instant from,
            Instant to,
            CurrencyCode reportingCurrency,
            PortfolioReplay.Result replay,
            Map<UUID, Lot> lotsAtFrom,
            Map<UUID, Lot> lotsAtEnd,
            Map<UUID, Activity> activitiesById,
            List<Activity> allActivities,
            CalculationAccumulator calculation
    ) {
        for (var consumption : replay.consumptions()) {
            Activity sale =
                    activitiesById.get(
                            consumption.sellActivityId()
                    );

            if (sale == null) {
                throw new IllegalStateException(
                        "No se encontró la SELL asociada a un LotConsumption."
                );
            }

            Instant saleAt =
                    sale.getOccurredAt();

            if (!saleAt.isAfter(from)
                    || saleAt.isAfter(to)) {
                continue;
            }

            Lot lot =
                    lotsAtEnd.get(
                            consumption.lotId()
                    );

            if (lot == null) {
                throw new IllegalStateException(
                        "No se encontró el Lot asociado a un LotConsumption."
                );
            }

            LotBase base =
                    resolveLotBase(
                            userId,
                            lot,
                            lotsAtFrom,
                            activitiesById,
                            allActivities,
                            from,
                            saleAt,
                            calculation
                    );

            if (base == null) {
                continue;
            }

            BigDecimal adjustedBasePrice =
                    adjustBasePriceForSplits(
                            lot.getAssetReference(),
                            base.unitPrice(),
                            base.baseAt(),
                            saleAt,
                            allActivities
                    );

            TradeActivityDetails saleDetails =
                    (TradeActivityDetails)
                            sale.getDetails();

            Money settlement =
                    saleDetails
                            .settlement()
                            .amount();

            BigDecimal allocatedProceeds =
                    settlement.amount()
                            .multiply(
                                    consumption.quantity()
                            )
                            .divide(
                                    saleDetails.quantity(),
                                    PRICE_SCALE,
                                    RoundingMode.HALF_UP
                            );

            Money proceedsMoney =
                    new Money(
                            allocatedProceeds,
                            settlement.currency()
                    );

            Money baseMoney =
                    new Money(
                            adjustedBasePrice
                                    .multiply(
                                            consumption.quantity()
                                    )
                                    .setScale(
                                            MONEY_SCALE,
                                            RoundingMode.HALF_UP
                                    ),
                            base.currency()
                    );

            /*
             * La liquidación está en settlement.currency().
             * Para llevarla a la moneda de reporting necesitamos:
             *
             * settlement currency -> reporting currency
             */
            FxQuote saleFx =
                    fxQuote(
                            settlement.currency(),
                            reportingCurrency,
                            saleAt
                    );

            if (saleFx == null) {
                calculation.missing(
                        "SELL_FX:"
                                + sale.getId()
                );
                continue;
            }

            BigDecimal proceedsLocal =
                    proceedsMoney.amount()
                            .multiply(
                                    saleFx.rate()
                            )
                            .setScale(
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );

            FxQuote baseFx =
                    fxQuote(
                            baseMoney.currency(),
                            reportingCurrency,
                            saleAt
                    );

            if (baseFx == null) {
                calculation.missing(
                        "SELL_COST_FX:"
                                + sale.getId()
                );
                continue;
            }

            BigDecimal baseLocal =
                    baseMoney.amount()
                            .multiply(
                                    baseFx.rate()
                            )
                            .setScale(
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );

            calculation.realizedGains =
                    calculation.realizedGains.add(
                            proceedsLocal.subtract(baseLocal)
                    );

            calculation.estimated |=
                    base.estimated();

            calculation.estimated |=
                    saleFx.estimated();

            calculation.estimated |=
                    baseFx.estimated();
        }
    }

    private void calculateOpenPositionEffects(
            UUID userId,
            Instant from,
            Instant to,
            CurrencyCode reportingCurrency,
            PortfolioReplay.Result endingReplay,
            Map<UUID, Lot> lotsAtFrom,
            Map<UUID, Activity> activitiesById,
            List<Activity> allActivities,
            CalculationAccumulator calculation
    ) {
        for (Lot lot : endingReplay.lots()) {
            if (lot.getRemainingQuantity().signum() == 0) {
                continue;
            }

            LotBase base =
                    resolveLotBase(
                            userId,
                            lot,
                            lotsAtFrom,
                            activitiesById,
                            allActivities,
                            from,
                            to,
                            calculation
                    );

            if (base == null) {
                continue;
            }

            BigDecimal adjustedBasePrice =
                    adjustBasePriceForSplits(
                            lot.getAssetReference(),
                            base.unitPrice(),
                            base.baseAt(),
                            to,
                            allActivities
                    );

            FxQuote baseFx =
                    fxQuote(
                            base.currency(),
                            reportingCurrency,
                            base.baseAt()
                    );

            if (baseFx == null) {
                calculation.missing(
                        "OPEN_POSITION_BASE_FX:"
                                + lot.getId()
                );
                continue;
            }

            PricePoint endingPrice =
                    priceAt(
                            userId,
                            lot.getAssetReference(),
                            to
                    );

            if (endingPrice == null) {
                calculation.missing(
                        "OPEN_POSITION_END_PRICE:"
                                + lot.getId()
                );
                continue;
            }

            FxQuote endingFx =
                    fxQuote(
                            endingPrice.currency(),
                            reportingCurrency,
                            to
                    );

            if (endingFx == null) {
                calculation.missing(
                        "OPEN_POSITION_END_FX:"
                                + lot.getId()
                );
                continue;
            }

            if (!base.currency().equals(
                    endingPrice.currency()
            )) {
                throw new IllegalStateException(
                        "La moneda base de un Lot no coincide "
                                + "con la moneda del precio del Asset."
                );
            }

            BigDecimal quantity =
                    lot.getRemainingQuantity();

            BigDecimal priceEffect =
                    quantity
                            .multiply(
                                    endingPrice.unitPrice()
                                            .subtract(
                                                    adjustedBasePrice
                                            )
                            )
                            .multiply(
                                    baseFx.rate()
                            )
                            .setScale(
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal fxEffect =
                    quantity
                            .multiply(
                                    endingPrice.unitPrice()
                            )
                            .multiply(
                                    endingFx.rate()
                                            .subtract(
                                                    baseFx.rate()
                                            )
                            )
                            .setScale(
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );

            calculation.unrealizedPriceEffect =
                    calculation.unrealizedPriceEffect
                            .add(priceEffect);

            calculation.fxEffect =
                    calculation.fxEffect
                            .add(fxEffect);

            calculation.estimated |=
                    base.estimated();

            calculation.estimated |=
                    baseFx.estimated();

            calculation.estimated |=
                    endingPrice.estimated();

            calculation.estimated |=
                    endingFx.estimated();

            calculation.estimated |=
                    lot.isSynthetic();
        }
    }

    private void calculateCashFxEffect(
            UUID userId,
            Instant from,
            Instant to,
            CurrencyCode reportingCurrency,
            Map<CurrencyCode, Money> openingCash,
            List<Activity> allActivities,
            CalculationAccumulator calculation
    ) {
        Map<CurrencyCode, Deque<CashTranche>> tranches =
                new LinkedHashMap<>();

        for (Money balance : openingCash.values()) {
            if (balance.amount().signum() <= 0) {
                continue;
            }

            FxQuote baseFx =
                    fxQuote(
                            balance.currency(),
                            reportingCurrency,
                            from
                    );

            if (baseFx == null) {
                calculation.missing(
                        "OPENING_CASH_FX:"
                                + balance.currency().value()
                );
                continue;
            }

            addCashTranche(
                    tranches,
                    balance.currency(),
                    balance.amount(),
                    baseFx
            );

            calculation.estimated |=
                    baseFx.estimated();
        }

        for (Activity activity : allActivities) {
            Instant occurredAt =
                    activity.getOccurredAt();

            if (!occurredAt.isAfter(from)
                    || occurredAt.isAfter(to)) {
                continue;
            }

            switch (activity.getType()) {
                case BUY -> {
                    TradeActivityDetails details =
                            (TradeActivityDetails)
                                    activity.getDetails();

                    consumeCash(
                            tranches,
                            details.settlement().amount(),
                            occurredAt,
                            reportingCurrency,
                            calculation
                    );
                }

                case SELL -> {
                    TradeActivityDetails details =
                            (TradeActivityDetails)
                                    activity.getDetails();

                    addCash(
                            tranches,
                            details.settlement().amount(),
                            occurredAt,
                            reportingCurrency,
                            calculation
                    );
                }

                case DEPOSIT -> {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    addCash(
                            tranches,
                            details.amount(),
                            occurredAt,
                            reportingCurrency,
                            calculation
                    );
                }

                case WITHDRAW -> {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    consumeCash(
                            tranches,
                            details.amount(),
                            occurredAt,
                            reportingCurrency,
                            calculation
                    );
                }

                case OPENING_CASH -> {
                    CashActivityDetails details =
                            (CashActivityDetails)
                                    activity.getDetails();

                    addCash(
                            tranches,
                            details.amount(),
                            occurredAt,
                            reportingCurrency,
                            calculation
                    );
                }

                case DIVIDEND -> {
                    if (activity.getDetails()
                            instanceof CashActivityDetails details) {

                        addCash(
                                tranches,
                                details.amount(),
                                occurredAt,
                                reportingCurrency,
                                calculation
                        );
                    }
                }

                case EXCHANGE -> {
                    ExchangeActivityDetails details =
                            (ExchangeActivityDetails)
                                    activity.getDetails();

                    consumeCash(
                            tranches,
                            details.origin(),
                            occurredAt,
                            reportingCurrency,
                            calculation
                    );

                    addCash(
                            tranches,
                            details.destination(),
                            occurredAt,
                            reportingCurrency,
                            calculation
                    );
                }

                default -> {
                    // SPLIT, OPENING_POSITION y
                    // DIVIDEND en unidades no afectan al cash.
                }
            }
        }

        for (Map.Entry<CurrencyCode, Deque<CashTranche>> entry :
                tranches.entrySet()) {

            CurrencyCode currency =
                    entry.getKey();

            Deque<CashTranche> queue =
                    entry.getValue();

            if (queue.isEmpty()) {
                continue;
            }

            FxQuote endingFx =
                    fxQuote(
                            currency,
                            reportingCurrency,
                            to
                    );

            if (endingFx == null) {
                calculation.missing(
                        "ENDING_CASH_FX:"
                                + currency.value()
                );
                continue;
            }

            for (CashTranche tranche : queue) {
                if (tranche.amount.signum() <= 0) {
                    continue;
                }

                calculation.fxEffect =
                        calculation.fxEffect.add(
                                tranche.amount
                                        .multiply(
                                                endingFx.rate()
                                                        .subtract(
                                                                tranche.baseFx.rate()
                                                        )
                                        )
                        );

                calculation.estimated |=
                        tranche.baseFx.estimated();

                calculation.estimated |=
                        endingFx.estimated();
            }
        }

        calculation.fxEffect =
                calculation.fxEffect.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private void addCash(
            Map<CurrencyCode, Deque<CashTranche>> tranches,
            Money amount,
            Instant occurredAt,
            CurrencyCode reportingCurrency,
            CalculationAccumulator calculation
    ) {
        FxQuote baseFx =
                fxQuote(
                        amount.currency(),
                        reportingCurrency,
                        occurredAt
                );

        if (baseFx == null) {
            calculation.missing(
                    "CASH_FLOW_FX:"
                            + amount.currency().value()
                            + ":"
                            + occurredAt
            );
            return;
        }

        addCashTranche(
                tranches,
                amount.currency(),
                amount.amount(),
                baseFx
        );

        calculation.estimated |=
                baseFx.estimated();
    }

    private void consumeCash(
            Map<CurrencyCode, Deque<CashTranche>> tranches,
            Money amount,
            Instant occurredAt,
            CurrencyCode reportingCurrency,
            CalculationAccumulator calculation
    ) {
        FxQuote eventFx =
                fxQuote(
                        amount.currency(),
                        reportingCurrency,
                        occurredAt
                );

        if (eventFx == null) {
            calculation.missing(
                    "CASH_CONSUMPTION_FX:"
                            + amount.currency().value()
                            + ":"
                            + occurredAt
            );
            return;
        }

        Deque<CashTranche> queue =
                tranches.computeIfAbsent(
                        amount.currency(),
                        ignored -> new ArrayDeque<>()
                );

        BigDecimal remaining =
                amount.amount();

        while (remaining.signum() > 0
                && !queue.isEmpty()) {

            CashTranche tranche =
                    queue.peekFirst();

            BigDecimal consumed =
                    remaining.min(
                            tranche.amount
                    );

            calculation.fxEffect =
                    calculation.fxEffect.add(
                            consumed
                                    .multiply(
                                            eventFx.rate()
                                                    .subtract(
                                                            tranche.baseFx.rate()
                                                    )
                                    )
                    );

            calculation.estimated |=
                    tranche.baseFx.estimated();

            tranche.amount =
                    tranche.amount.subtract(
                            consumed
                    );

            remaining =
                    remaining.subtract(
                            consumed
                    );

            if (tranche.amount.signum() == 0) {
                queue.removeFirst();
            }
        }

        if (remaining.signum() > 0) {
            throw new IllegalStateException(
                    "El consumo de Investment Cash excede "
                            + "el saldo disponible para "
                            + amount.currency().value()
            );
        }

        calculation.estimated |=
                eventFx.estimated();
    }

    private void addCashTranche(
            Map<CurrencyCode, Deque<CashTranche>> tranches,
            CurrencyCode currency,
            BigDecimal amount,
            FxQuote baseFx
    ) {
        if (amount.signum() <= 0) {
            return;
        }

        tranches
                .computeIfAbsent(
                        currency,
                        ignored -> new ArrayDeque<>()
                )
                .addLast(
                        new CashTranche(
                                amount,
                                baseFx
                        )
                );
    }

    private LotBase resolveLotBase(
            UUID userId,
            Lot lot,
            Map<UUID, Lot> lotsAtFrom,
            Map<UUID, Activity> activitiesById,
            List<Activity> allActivities,
            Instant from,
            Instant endpoint,
            CalculationAccumulator calculation
    ) {
        Lot historicalLot =
                lotsAtFrom.get(
                        lot.getId()
                );

        CurrencyCode assetCurrency =
                assetCurrency(
                        userId,
                        lot.getAssetReference()
                );

        if (historicalLot != null
                && historicalLot.getRemainingQuantity().signum() > 0) {

            PricePoint price =
                    priceAt(
                            userId,
                            lot.getAssetReference(),
                            from
                    );

            if (price == null) {
                calculation.missing(
                        "LOT_BASE_PRICE_FROM:"
                                + lot.getId()
                );
                return null;
            }

            if (!assetCurrency.equals(
                    price.currency()
            )) {
                throw new IllegalStateException(
                        "La moneda del precio histórico no coincide "
                                + "con la moneda del Asset."
                );
            }

            return new LotBase(
                    price.unitPrice(),
                    from,
                    assetCurrency,
                    price.estimated()
            );
        }

        if (lot.getSource().type()
                == LotSourceType.HOLDING) {

            return resolveHoldingLotBase(
                    lot,
                    assetCurrency
            );
        }

        Activity sourceActivity =
                activitiesById.get(
                        lot.getSource().id()
                );

        if (sourceActivity == null) {
            throw new IllegalStateException(
                    "No se encontró la Activity origen del Lot "
                            + lot.getId()
            );
        }

        BigDecimal unitPrice;

        switch (sourceActivity.getType()) {
            case BUY -> {
                TradeActivityDetails details =
                        (TradeActivityDetails)
                                sourceActivity.getDetails();

                unitPrice =
                        details.unitPrice().amount();
            }

            case OPENING_POSITION -> {
                OpeningPositionDetails details =
                        (OpeningPositionDetails)
                                sourceActivity.getDetails();

                unitPrice =
                        details.acquisitionCost()
                                .amount()
                                .divide(
                                        details.quantity(),
                                        PRICE_SCALE,
                                        RoundingMode.HALF_UP
                                );
            }

            case DIVIDEND -> {
                if (!(sourceActivity.getDetails()
                        instanceof DividendUnitsDetails details)) {

                    throw new IllegalStateException(
                            "Un Lot procedente de DIVIDEND "
                                    + "debe ser un dividendo en unidades."
                    );
                }

                unitPrice =
                        details.referenceUnitPrice()
                                .amount();
            }

            default -> throw new IllegalStateException(
                    "Activity incompatible como origen de Lot: "
                            + sourceActivity.getType()
            );
        }

        CurrencyCode sourceCurrency =
                sourceCurrency(
                        sourceActivity
                );

        if (!assetCurrency.equals(sourceCurrency)) {
            throw new IllegalStateException(
                    "La moneda de la base económica del Lot "
                            + "no coincide con la moneda del Asset."
            );
        }

        return new LotBase(
                unitPrice,
                sourceActivity.getOccurredAt(),
                assetCurrency,
                false
        );
    }

    private LotBase resolveHoldingLotBase(
            Lot lot,
            CurrencyCode assetCurrency
    ) {
        if (!lot.isSynthetic()) {
            throw new IllegalStateException(
                    "El Lot con origen HOLDING debe ser synthetic."
            );
        }

        return new LotBase(
                lot.getTotalCost()
                        .amount()
                        .divide(
                                lot.getOriginalQuantity(),
                                PRICE_SCALE,
                                RoundingMode.HALF_UP
                        ),
                lot.getAcquiredAt(),
                assetCurrency,
                false
        );
    }

    private CurrencyCode sourceCurrency(
            Activity activity
    ) {
        return switch (activity.getType()) {
            case BUY -> {
                TradeActivityDetails details =
                        (TradeActivityDetails)
                                activity.getDetails();

                yield details.unitPrice().currency();
            }

            case OPENING_POSITION -> {
                OpeningPositionDetails details =
                        (OpeningPositionDetails)
                                activity.getDetails();

                yield details.acquisitionCost().currency();
            }

            case DIVIDEND -> {
                DividendUnitsDetails details =
                        (DividendUnitsDetails)
                                activity.getDetails();

                yield details.referenceUnitPrice()
                        .currency();
            }

            default -> throw new IllegalStateException(
                    "Activity no compatible con un Lot: "
                            + activity.getType()
            );
        };
    }

    private BigDecimal adjustBasePriceForSplits(
            AssetReference assetReference,
            BigDecimal basePrice,
            Instant baseAt,
            Instant until,
            List<Activity> activities
    ) {
        BigDecimal splitFactor =
                BigDecimal.ONE;

        for (Activity activity : activities) {
            if (activity.getType()
                    != ActivityType.SPLIT) {
                continue;
            }

            if (!assetReference.equals(
                    activity.getAssetReference()
            )) {
                continue;
            }

            if (!activity.getOccurredAt()
                    .isAfter(baseAt)) {
                continue;
            }

            if (activity.getOccurredAt()
                    .isAfter(until)) {
                continue;
            }

            BigDecimal ratio =
                    ((com.puntomartinez.millete.investments.domain.model.SplitActivityDetails)
                            activity.getDetails())
                            .ratio();

            splitFactor =
                    splitFactor.multiply(
                            ratio
                    );
        }

        return basePrice
                .divide(
                        splitFactor,
                        PRICE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private PricePoint priceAt(
            UUID userId,
            AssetReference reference,
            Instant at
    ) {
        return switch (reference.kind()) {
            case SHARED -> {
                SharedAsset asset =
                        sharedAssets.findById(
                                reference.id()
                        ).orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "SharedAsset no encontrado."
                                )
                        );

                AssetPrice price =
                        assetPrices
                                .findLatestAt(
                                        reference.id(),
                                        at
                                )
                                .orElse(null);

                if (price == null) {
                    yield null;
                }

                BigDecimal valuationPrice =
                        price.valuationPrice();

                if (valuationPrice == null) {
                    yield null;
                }

                if (!asset.getCurrency().equals(
                        price.currency()
                )) {
                    throw new IllegalStateException(
                            "La moneda del precio no coincide "
                                    + "con la moneda del SharedAsset."
                    );
                }

                yield new PricePoint(
                        valuationPrice,
                        price.currency(),
                        price.timestamp(),
                        !price.timestamp().equals(at)
                );
            }

            case USER -> {
                UserAsset asset =
                        userAssets
                                .findByIdAndUserId(
                                        reference.id(),
                                        userId
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "UserAsset no encontrado."
                                        )
                                );

                UserAssetPrice price =
                        userAssetPrices
                                .findLatestAt(
                                        reference.id(),
                                        at
                                )
                                .orElse(null);

                if (price == null) {
                    yield null;
                }

                Money unitPrice =
                        price.getUnitPrice();

                if (!asset.getCurrency().equals(
                        unitPrice.currency()
                )) {
                    throw new IllegalStateException(
                            "La moneda del precio no coincide "
                                    + "con la moneda del UserAsset."
                    );
                }

                yield new PricePoint(
                        unitPrice.amount(),
                        unitPrice.currency(),
                        price.getTimestamp(),
                        !price.getTimestamp().equals(at)
                );
            }
        };
    }

    private CurrencyCode assetCurrency(
            UUID userId,
            AssetReference reference
    ) {
        return switch (reference.kind()) {
            case SHARED ->
                    sharedAssets
                            .findById(
                                    reference.id()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "SharedAsset no encontrado."
                                    )
                            )
                            .getCurrency();

            case USER ->
                    userAssets
                            .findByIdAndUserId(
                                    reference.id(),
                                    userId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "UserAsset no encontrado."
                                    )
                            )
                            .getCurrency();
        };
    }

    private FxQuote fxQuote(
            CurrencyCode from,
            CurrencyCode to,
            Instant at
    ) {
        if (from.equals(to)) {
            return new FxQuote(
                    BigDecimal.ONE,
                    at,
                    "IDENTITY",
                    false
            );
        }

        Optional<FxRate> direct =
                fxRates.findLatestAt(
                        from,
                        to,
                        at
                );

        Optional<FxRate> inverse =
                fxRates.findLatestAt(
                        to,
                        from,
                        at
                );

        if (direct.isPresent()
                && (
                inverse.isEmpty()
                        || !inverse.get().timestamp()
                        .isAfter(
                                direct.get().timestamp()
                        )
        )) {
            FxRate rate =
                    direct.get();

            return new FxQuote(
                    rate.rate(),
                    rate.timestamp(),
                    rate.source(),
                    !rate.timestamp().equals(at)
            );
        }

        if (inverse.isPresent()) {
            FxRate rate =
                    inverse.get();

            return new FxQuote(
                    BigDecimal.ONE.divide(
                            rate.rate(),
                            PRICE_SCALE,
                            RoundingMode.HALF_UP
                    ),
                    rate.timestamp(),
                    "INVERSE:" + rate.source(),
                    !rate.timestamp().equals(at)
            );
        }

        return null;
    }

    private Optional<ConvertedAmount> convertToReporting(
            Money money,
            CurrencyCode reportingCurrency,
            Instant at
    ) {
        if (money.currency().equals(
                reportingCurrency
        )) {
            return Optional.of(
                    new ConvertedAmount(
                            money.amount(),
                            false
                    )
            );
        }

        FxQuote quote =
                fxQuote(
                        money.currency(),
                        reportingCurrency,
                        at
                );

        if (quote == null) {
            return Optional.empty();
        }

        BigDecimal amount =
                money.amount()
                        .multiply(
                                quote.rate()
                        )
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

        return Optional.of(
                new ConvertedAmount(
                        amount,
                        quote.estimated()
                )
        );
    }

    private void addConvertedAmount(
            Money amount,
            Instant at,
            CurrencyCode reportingCurrency,
            CalculationAccumulator calculation,
            FlowType type,
            Activity activity
    ) {
        Optional<ConvertedAmount> converted =
                convertToReporting(
                        amount,
                        reportingCurrency,
                        at
                );

        if (converted.isEmpty()) {
            calculation.missing(
                    type.name()
                            + "_FX:"
                            + activity.getId()
            );
            return;
        }

        switch (type) {
            case OPENING_CAPITAL ->
                    calculation.openingCapital =
                            calculation.openingCapital.add(
                                    converted.get().amount()
                            );

            case CONTRIBUTION ->
                    calculation.contributions =
                            calculation.contributions.add(
                                    converted.get().amount()
                            );

            case WITHDRAWAL ->
                    calculation.withdrawals =
                            calculation.withdrawals.add(
                                    converted.get().amount()
                            );

            case CASH_DIVIDEND ->
                    calculation.cashDividends =
                            calculation.cashDividends.add(
                                    converted.get().amount()
                            );

            case IN_KIND_DIVIDEND ->
                    calculation.inKindDividends =
                            calculation.inKindDividends.add(
                                    converted.get().amount()
                            );
        }

        calculation.estimated |=
                converted.get().estimated();
    }

    private Money money(
            BigDecimal amount,
            CurrencyCode currency
    ) {
        return new Money(
                amount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                ),
                currency
        );
    }

    private Comparator<Activity> activityOrder() {
        return Comparator
                .comparing(Activity::getOccurredAt)
                .thenComparingLong(Activity::getOrderingKey)
                .thenComparing(
                        activity ->
                                activity.getId().toString()
                );
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }
    }

    private void validatePeriod(
            Instant from,
            Instant to
    ) {
        if (from == null
                || to == null
                || !from.isBefore(to)) {

            throw new IllegalArgumentException(
                    "from debe ser anterior a to."
            );
        }
    }

    private enum FlowType {
        OPENING_CAPITAL,
        CONTRIBUTION,
        WITHDRAWAL,
        CASH_DIVIDEND,
        IN_KIND_DIVIDEND
    }

    private record PricePoint(
            BigDecimal unitPrice,
            CurrencyCode currency,
            Instant timestamp,
            boolean estimated
    ) {
    }

    private record FxQuote(
            BigDecimal rate,
            Instant timestamp,
            String source,
            boolean estimated
    ) {
    }

    private record ConvertedAmount(
            BigDecimal amount,
            boolean estimated
    ) {
    }

    private record LotBase(
            BigDecimal unitPrice,
            Instant baseAt,
            CurrencyCode currency,
            boolean estimated
    ) {
    }

    private static final class CashTranche {

        private BigDecimal amount;
        private final FxQuote baseFx;

        private CashTranche(
                BigDecimal amount,
                FxQuote baseFx
        ) {
            this.amount = amount;
            this.baseFx = baseFx;
        }
    }

    private static final class CalculationAccumulator {

        private BigDecimal openingCapital =
                BigDecimal.ZERO;

        private BigDecimal contributions =
                BigDecimal.ZERO;

        private BigDecimal withdrawals =
                BigDecimal.ZERO;

        private BigDecimal realizedGains =
                BigDecimal.ZERO;

        private BigDecimal unrealizedPriceEffect =
                BigDecimal.ZERO;

        private BigDecimal fxEffect =
                BigDecimal.ZERO;

        private BigDecimal cashDividends =
                BigDecimal.ZERO;

        private BigDecimal inKindDividends =
                BigDecimal.ZERO;

        private boolean estimated;

        private final List<String> missing =
                new ArrayList<>();

        private void missing(String reason) {
            if (reason != null
                    && !reason.isBlank()) {
                missing.add(reason);
            }
        }

        private boolean hasMissingData() {
            return !missing.isEmpty();
        }

        private String unavailableReason() {
            return missing.stream()
                    .distinct()
                    .reduce(
                            (left, right) ->
                                    left + "," + right
                    )
                    .orElse(
                            "PERFORMANCE_DATA_UNAVAILABLE"
                    );
        }
    }
}