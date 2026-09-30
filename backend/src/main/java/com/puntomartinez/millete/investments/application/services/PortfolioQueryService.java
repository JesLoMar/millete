package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CalculationStatus;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.HoldingStatus;
import com.puntomartinez.millete.investments.domain.model.Lot;
import com.puntomartinez.millete.investments.domain.model.LotConsumption;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.PortfolioReplay;
import com.puntomartinez.millete.investments.domain.model.Position;
import com.puntomartinez.millete.investments.domain.model.PositionValuation;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentCashUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetPortfolioAtUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListClosedLotsUseCase;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PortfolioQueryService implements
        GetInvestmentCashUseCase,
        GetPortfolioAtUseCase,
        ListClosedLotsUseCase {

    private static final int AMOUNT_SCALE = 12;

    private final ActivityRepository activities;
    private final HoldingRepository holdings;
    private final InvestmentTrackingSettingsRepository trackingSettings;
    private final SharedAssetRepository sharedAssets;
    private final UserAssetRepository userAssets;
    private final AssetPriceRepository assetPrices;
    private final UserAssetPriceRepository userAssetPrices;
    private final FxRateRepository fxRates;
    private final UserCurrencyPort userCurrencies;

    public PortfolioQueryService(
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
    public Map<String, BigDecimal> get(
            UUID userId,
            Instant asOf
    ) {
        validateUserId(userId);
        validateAsOf(asOf);

        PortfolioReplay.Result replay =
                replayAt(
                        userId,
                        asOf
                );

        Map<String, BigDecimal> result =
                new LinkedHashMap<>();

        replay.cashBalances()
                .forEach((currency, money) ->
                        result.put(
                                currency.value(),
                                money.amount()
                        )
                );

        return Map.copyOf(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResult get(
            UUID userId,
            Instant asOf
    ) {
        validateUserId(userId);
        validateAsOf(asOf);

        CurrencyCode localCurrency =
                userCurrencies
                        .currencyAt(
                                userId,
                                asOf
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe moneda local para el instante solicitado."
                                )
                        );

        PortfolioReplay.Result replay =
                replayAt(
                        userId,
                        asOf
                );

        Map<String, BigDecimal> cashBalances =
                mapCashBalances(
                        replay.cashBalances()
                );

        CashValuationResult cashValuation =
                valueCashBalances(
                        replay.cashBalances(),
                        localCurrency,
                        asOf
                );

        BigDecimal totalInLocalCurrency =
                cashValuation.total();

        boolean estimated =
                cashValuation.estimated();

        List<PositionResult> positionResults =
                new ArrayList<>();

        for (Position position : replay.positions()) {

            PositionResult result =
                    mapPosition(
                            position,
                            localCurrency,
                            asOf
                    );

            positionResults.add(result);

            if (result.calculationStatus()
                    == CalculationStatus.CALCULABLE) {

                totalInLocalCurrency =
                        totalInLocalCurrency.add(
                                result.marketValue()
                        );
            } else {
                estimated = true;
            }

            estimated |= result.estimated();
        }

        return new PortfolioResult(
                asOf,
                localCurrency.value(),
                cashBalances,
                List.copyOf(positionResults),
                totalInLocalCurrency,
                estimated
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClosedLotResult> list(
            UUID userId,
            Instant from,
            Instant to
    ) {
        validateUserId(userId);
        validatePeriod(from, to);

        CurrencyCode localCurrency =
                userCurrencies
                        .currencyAt(
                                userId,
                                to
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe moneda local para el final del período."
                                )
                        );

        PortfolioReplay.Result replay =
                replayAt(
                        userId,
                        to
                );

        List<Activity> userActivities =
                activities.findAllByUserId(userId)
                        .stream()
                        .filter(activity ->
                                !activity.getOccurredAt()
                                        .isAfter(to)
                        )
                        .toList();

        Map<UUID, Activity> activitiesById =
                new LinkedHashMap<>();

        for (Activity activity : userActivities) {
            activitiesById.put(
                    activity.getId(),
                    activity
            );
        }

        List<ClosedLotResult> result =
                new ArrayList<>();

        for (Lot lot : replay.lots()) {

            if (lot.getRemainingQuantity().signum() != 0) {
                continue;
            }

            List<LotConsumption> lotConsumptions =
                    replay.consumptions()
                            .stream()
                            .filter(consumption ->
                                    consumption.lotId()
                                            .equals(lot.getId())
                            )
                            .toList();

            if (lotConsumptions.isEmpty()) {
                continue;
            }

            Instant closedAt = null;

            Money costBasis =
                    Money.zero(
                            lot.getTotalCost().currency()
                    );

            Money proceedsInLocal =
                    Money.zero(localCurrency);

            for (LotConsumption consumption : lotConsumptions) {

                Activity sell =
                        activitiesById.get(
                                consumption.sellActivityId()
                        );

                if (sell == null) {
                    throw new IllegalStateException(
                            "No se encontró la SELL asociada al LotConsumption."
                    );
                }

                if (closedAt == null
                        || sell.getOccurredAt()
                        .isAfter(closedAt)) {

                    closedAt =
                            sell.getOccurredAt();
                }

                costBasis =
                        costBasis.add(
                                consumption.costBasis()
                        );

                TradeActivityDetails details =
                        (TradeActivityDetails)
                                sell.getDetails();

                Money settlementAmount =
                        details
                                .settlement()
                                .amount();

                BigDecimal allocatedProceedsAmount =
                        settlementAmount
                                .amount()
                                .multiply(
                                        consumption.quantity()
                                )
                                .divide(
                                        details.quantity(),
                                        AMOUNT_SCALE,
                                        RoundingMode.HALF_UP
                                );

                Money allocatedProceeds =
                        new Money(
                                allocatedProceedsAmount,
                                settlementAmount.currency()
                        );

                Money proceedsLocal =
                        convertToLocal(
                                allocatedProceeds,
                                localCurrency,
                                sell.getOccurredAt()
                        ).orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe FX para valorar los proceeds de la venta en moneda local."
                                )
                        );

                proceedsInLocal =
                        proceedsInLocal.add(
                                proceedsLocal
                        );
            }

            if (closedAt == null
                    || closedAt.isBefore(from)
                    || closedAt.isAfter(to)) {

                continue;
            }

            Money costBasisInLocal =
                    convertToLocal(
                            costBasis,
                            localCurrency,
                            closedAt
                    ).orElseThrow(() ->
                            new IllegalStateException(
                                    "No existe FX para valorar el coste realizado en moneda local."
                            )
                    );

            Money realizedGain =
                    proceedsInLocal.subtract(
                            costBasisInLocal
                    );

            result.add(
                    new ClosedLotResult(
                            lot.getId(),
                            lot.getAssetReference(),
                            lot.getOriginalQuantity(),
                            costBasis.amount(),
                            costBasis.currency().value(),
                            proceedsInLocal.amount(),
                            localCurrency.value(),
                            realizedGain.amount(),
                            localCurrency.value(),
                            lot.getAcquiredAt(),
                            closedAt,
                            lot.isSynthetic(),
                            lot.isSynthetic()
                    )
            );
        }

        return List.copyOf(result);
    }

    private PortfolioReplay.Result replayAt(
            UUID userId,
            Instant asOf
    ) {
        List<Activity> userActivities =
                activities.findAllByUserId(userId)
                        .stream()
                        .filter(activity ->
                                !activity.getOccurredAt()
                                        .isAfter(asOf)
                        )
                        .toList();

        List<Holding> userHoldings =
                holdings.findAllByUserId(userId)
                        .stream()
                        .filter(holding ->
                                holding.getStatus()
                                        == HoldingStatus.ACTIVE
                        )
                        .filter(holding ->
                                !holding.getSnapshotAt()
                                        .isAfter(asOf)
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

        return PortfolioReplay.replay(
                userId,
                userActivities,
                userHoldings,
                trackingStartAt
        );
    }

    private PositionResult mapPosition(
            Position position,
            CurrencyCode localCurrency,
            Instant asOf
    ) {
        PositionValuation valuation =
                valuePosition(
                        position,
                        localCurrency,
                        asOf
                );

        BigDecimal localCostBasis =
                convertToLocal(
                        position.acquisitionCost(),
                        localCurrency,
                        asOf
                )
                        .map(Money::amount)
                        .orElse(null);

        if (valuation.status()
                == CalculationStatus.NOT_CALCULABLE) {

            return new PositionResult(
                    position.assetReference(),
                    position.quantity(),
                    localCostBasis,
                    null,
                    null,
                    localCurrency.value(),
                    CalculationStatus.NOT_CALCULABLE,
                    false,
                    position.historyIncomplete(),
                    valuation.unavailableReason()
            );
        }

        BigDecimal marketValue =
                valuation.value().amount();

        BigDecimal unrealizedGain = null;

        if (localCostBasis != null) {
            unrealizedGain =
                    marketValue.subtract(
                            localCostBasis
                    );
        }

        return new PositionResult(
                position.assetReference(),
                position.quantity(),
                localCostBasis,
                marketValue,
                unrealizedGain,
                localCurrency.value(),
                CalculationStatus.CALCULABLE,
                valuation.estimated(),
                position.historyIncomplete(),
                null
        );
    }

    private PositionValuation valuePosition(
            Position position,
            CurrencyCode localCurrency,
            Instant asOf
    ) {
        AssetReference reference =
                position.assetReference();

        CurrencyCode assetCurrency;
        BigDecimal unitPrice;

        switch (reference.kind()) {

            case SHARED -> {
                SharedAsset asset =
                        sharedAssets.findById(
                                        reference.id()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "SharedAsset no encontrado."
                                        )
                                );

                assetCurrency =
                        asset.getCurrency();

                AssetPrice price =
                        assetPrices.findLatestAt(
                                        reference.id(),
                                        asOf
                                )
                                .orElse(null);

                if (price == null) {
                    return PositionValuation.notCalculable(
                            "No existe un precio de mercado disponible para el instante solicitado."
                    );
                }

                unitPrice =
                        price.valuationPrice();

                if (unitPrice == null) {
                    return PositionValuation.notCalculable(
                            "El precio de mercado no contiene un valor de valoración."
                    );
                }

                if (!assetCurrency.equals(
                        price.currency()
                )) {
                    return PositionValuation.notCalculable(
                            "La moneda del precio no coincide con la moneda del Asset."
                    );
                }
            }

            case USER -> {
                UserAsset asset =
                        userAssets.findByIdAndUserId(
                                        reference.id(),
                                        position.userId()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "UserAsset no encontrado."
                                        )
                                );

                assetCurrency =
                        asset.getCurrency();

                UserAssetPrice price =
                        userAssetPrices.findLatestAt(
                                        reference.id(),
                                        asOf
                                )
                                .orElse(null);

                if (price == null) {
                    return PositionValuation.notCalculable(
                            "No existe una valoración del UserAsset aplicable al instante solicitado."
                    );
                }

                Money priceMoney =
                        price.getUnitPrice();

                unitPrice =
                        priceMoney.amount();

                if (!assetCurrency.equals(
                        priceMoney.currency()
                )) {
                    return PositionValuation.notCalculable(
                            "La moneda del precio no coincide con la moneda del UserAsset."
                    );
                }
            }

            default ->
                    throw new IllegalStateException(
                            "Tipo de AssetReference no soportado."
                    );
        }

        Money valueInAssetCurrency =
                new Money(
                        unitPrice,
                        assetCurrency
                ).multiply(
                        position.quantity(),
                        AMOUNT_SCALE
                );

        Optional<Money> valueInLocal =
                convertToLocal(
                        valueInAssetCurrency,
                        localCurrency,
                        asOf
                );

        if (valueInLocal.isEmpty()) {
            return PositionValuation.notCalculable(
                    "No existe un tipo de cambio disponible para convertir la posición a moneda local."
            );
        }

        return PositionValuation.calculable(
                valueInLocal.get(),
                position.estimated()
        );
    }

    private CashValuationResult valueCashBalances(
            Map<CurrencyCode, Money> cashBalances,
            CurrencyCode localCurrency,
            Instant asOf
    ) {
        Money total =
                Money.zero(localCurrency);

        boolean estimated = false;

        for (Money balance : cashBalances.values()) {

            Optional<Money> localValue =
                    convertToLocal(
                            balance,
                            localCurrency,
                            asOf
                    );

            if (localValue.isEmpty()) {
                estimated = true;
                continue;
            }

            total =
                    total.add(
                            localValue.get()
                    );
        }

        return new CashValuationResult(
                total.amount(),
                estimated
        );
    }

    private Optional<Money> convertToLocal(
            Money money,
            CurrencyCode localCurrency,
            Instant at
    ) {
        if (money.currency()
                .equals(localCurrency)) {

            return Optional.of(money);
        }

        Optional<FxRate> direct =
                fxRates.findLatestAt(
                        money.currency(),
                        localCurrency,
                        at
                );

        if (direct.isPresent()) {
            return Optional.of(
                    money.multiply(
                            direct.get().rate(),
                            AMOUNT_SCALE
                    )
            );
        }

        Optional<FxRate> inverse =
                fxRates.findLatestAt(
                        localCurrency,
                        money.currency(),
                        at
                );

        if (inverse.isPresent()) {
            return Optional.of(
                    money.divide(
                            inverse.get().rate(),
                            AMOUNT_SCALE
                    )
            );
        }

        return Optional.empty();
    }

    private Map<String, BigDecimal> mapCashBalances(
            Map<CurrencyCode, Money> balances
    ) {
        Map<String, BigDecimal> result =
                new LinkedHashMap<>();

        balances.forEach((currency, money) ->
                result.put(
                        currency.value(),
                        money.amount()
                )
        );

        return Map.copyOf(result);
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }
    }

    private void validateAsOf(Instant asOf) {
        if (asOf == null) {
            throw new IllegalArgumentException(
                    "asOf es obligatorio."
            );
        }
    }

    private void validatePeriod(
            Instant from,
            Instant to
    ) {
        if (from == null
                || to == null) {

            throw new IllegalArgumentException(
                    "from y to son obligatorios."
            );
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "from no puede ser posterior a to."
            );
        }
    }

    private record CashValuationResult(
            BigDecimal total,
            boolean estimated
    ) {
    }
}