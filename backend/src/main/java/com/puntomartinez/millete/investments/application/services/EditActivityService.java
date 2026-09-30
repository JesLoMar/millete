package com.puntomartinez.millete.investments.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityAudit;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AppliedFxRate;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.SettlementCurrency;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeSettlement;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.ports.in.EditActivityUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityAuditRepository;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityOrderingPort;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
public class EditActivityService
        implements EditActivityUseCase {

    private static final int AMOUNT_SCALE = 12;

    private final ActivityRepository activities;
    private final ActivityAuditRepository audits;
    private final ActivityOrderingPort ordering;
    private final InvestmentPortfolioLockPort portfolioLock;
    private final FxRateRepository fxRates;
    private final SharedAssetRepository sharedAssets;
    private final UserAssetRepository userAssets;
    private final UserCurrencyPort userCurrencies;
    private final PortfolioRebuildService portfolioRebuild;
    private final TimeProvider time;
    private final ObjectMapper mapper;

    public EditActivityService(
            ActivityRepository activities,
            ActivityAuditRepository audits,
            ActivityOrderingPort ordering,
            InvestmentPortfolioLockPort portfolioLock,
            FxRateRepository fxRates,
            SharedAssetRepository sharedAssets,
            UserAssetRepository userAssets,
            UserCurrencyPort userCurrencies,
            PortfolioRebuildService portfolioRebuild,
            TimeProvider time,
            ObjectMapper mapper
    ) {
        this.activities = activities;
        this.audits = audits;
        this.ordering = ordering;
        this.portfolioLock = portfolioLock;
        this.fxRates = fxRates;
        this.sharedAssets = sharedAssets;
        this.userAssets = userAssets;
        this.userCurrencies = userCurrencies;
        this.portfolioRebuild = portfolioRebuild;
        this.time = time;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Activity edit(
            UUID userId,
            UUID activityId,
            EditActivityCommand command
    ) {
        portfolioLock.lock(userId);

        Activity activity =
                activities.findByIdAndUserId(
                        activityId,
                        userId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Activity no encontrada"
                        )
                );

        validateCommand(command);

        if (activity.isLinkedCapitalMovement()) {
            throw new InvalidInputException(
                    "Una DEPOSIT o WITHDRAW vinculada a una Transaction no puede editarse."
            );
        }

        Instant newOccurredAt =
                command.occurredAt();

        long newOrderingKey =
                newOccurredAt.equals(activity.getOccurredAt())
                        ? activity.getOrderingKey()
                        : ordering.nextOrderingKey(
                                userId,
                                newOccurredAt
                        );

        String beforeJson = toJson(activity);

        ActivityDetailsResult newDetails =
                buildDetails(
                        userId,
                        activity,
                        command.details(),
                        newOccurredAt
                );

        activity.edit(
                time,
                newOccurredAt,
                newOrderingKey,
                newDetails.details(),
                newDetails.comment()
        );

        Activity saved =
                activities.save(activity);

        audits.save(
                new ActivityAudit(
                        UUID.randomUUID(),
                        saved.getId(),
                        userId,
                        beforeJson,
                        toJson(saved),
                        command.reason(),
                        time.now()
                )
        );

        portfolioRebuild.rebuild(userId);

        return saved;
    }

    private ActivityDetailsResult buildDetails(
            UUID userId,
            Activity activity,
            ActivityEditDetails requestedDetails,
            Instant newOccurredAt
    ) {
        return switch (activity.getType()) {

            case BUY -> buildTradeDetails(
                    userId,
                    activity,
                    requestedDetails,
                    newOccurredAt,
                    ActivityType.BUY
            );

            case SELL -> buildTradeDetails(
                    userId,
                    activity,
                    requestedDetails,
                    newOccurredAt,
                    ActivityType.SELL
            );

            case DIVIDEND -> buildDividendDetails(
                    activity,
                    requestedDetails
            );

            case SPLIT -> buildSplitDetails(
                    activity,
                    requestedDetails
            );

            case EXCHANGE -> buildExchangeDetails(
                    activity,
                    requestedDetails,
                    newOccurredAt
            );

            case DEPOSIT, WITHDRAW, OPENING_CASH, OPENING_POSITION ->
                    throw new InvalidInputException(
                            "La Activity " + activity.getType()
                                    + " no admite esta edición."
                    );
        };
    }

    private ActivityDetailsResult buildTradeDetails(
            UUID userId,
            Activity activity,
            ActivityEditDetails requestedDetails,
            Instant newOccurredAt,
            ActivityType type
    ) {
        if (!(requestedDetails instanceof BuySellDetails details)) {
            throw new InvalidInputException(
                    "La edición recibida no es válida para "
                            + type
            );
        }

        AssetReference assetReference =
                activity.getAssetReference();

        CurrencyCode assetCurrency =
                resolveAssetCurrency(
                        userId,
                        assetReference
                );

        CurrencyCode localCurrency =
                userCurrencies
                        .currencyAt(
                                userId,
                                newOccurredAt
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe moneda local para el instante de la operación."
                                )
                        );

        if (details.settlementCurrency() == null) {
            throw new InvalidInputException(
                    "La moneda de liquidación es obligatoria."
            );
        }

        if (details.quantity() == null
                || details.quantity().signum() <= 0) {
            throw new InvalidInputException(
                    "La cantidad debe ser positiva."
            );
        }

        if (details.unitPrice() == null
                || details.unitPrice().signum() <= 0) {
            throw new InvalidInputException(
                    "El precio unitario debe ser positivo."
            );
        }

        TradeActivityDetails currentDetails =
                (TradeActivityDetails)
                        activity.getDetails();

        AppliedFxRate appliedFxRate =
                resolveTradeFx(
                        type,
                        activity,
                        currentDetails,
                        details.settlementCurrency(),
                        assetCurrency,
                        localCurrency,
                        newOccurredAt
                );

        CurrencyCode settlementCurrency =
                details.settlementCurrency()
                        == SettlementCurrency.LOCAL
                        ? localCurrency
                        : assetCurrency;

        Money settlementUnitPrice =
                new Money(
                        details.unitPrice(),
                        settlementCurrency
                );

        Money settlementAmount =
                settlementUnitPrice.multiply(
                        details.quantity(),
                        AMOUNT_SCALE
                );

        Money assetUnitPrice;

        if (details.settlementCurrency()
                == SettlementCurrency.ASSET) {

            assetUnitPrice =
                    new Money(
                            details.unitPrice(),
                            assetCurrency
                    );

        } else if (assetCurrency.equals(localCurrency)) {

            assetUnitPrice =
                    new Money(
                            details.unitPrice(),
                            assetCurrency
                    );

        } else if (type == ActivityType.BUY) {

            assetUnitPrice =
                    settlementUnitPrice.multiply(
                            appliedFxRate.rate(),
                            AMOUNT_SCALE
                    );

        } else {

            assetUnitPrice =
                    settlementUnitPrice.divide(
                            appliedFxRate.rate(),
                            AMOUNT_SCALE
                    );
        }

        TradeSettlement settlement =
                new TradeSettlement(
                        settlementAmount,
                        appliedFxRate
                );

        return new ActivityDetailsResult(
                new TradeActivityDetails(
                        details.quantity(),
                        assetUnitPrice,
                        settlement
                ),
                details.comment()
        );
    }

    private AppliedFxRate resolveTradeFx(
            ActivityType type,
            Activity activity,
            TradeActivityDetails currentDetails,
            SettlementCurrency settlementCurrency,
            CurrencyCode assetCurrency,
            CurrencyCode localCurrency,
            Instant newOccurredAt
    ) {
        if (settlementCurrency
                == SettlementCurrency.ASSET) {
            return null;
        }

        if (assetCurrency.equals(localCurrency)) {
            return null;
        }

        CurrencyCode expectedBase =
                type == ActivityType.BUY
                        ? localCurrency
                        : assetCurrency;

        CurrencyCode expectedQuote =
                type == ActivityType.BUY
                        ? assetCurrency
                        : localCurrency;

        boolean occurredAtUnchanged =
                newOccurredAt.equals(
                        activity.getOccurredAt()
                );

        AppliedFxRate existingFx =
                currentDetails
                        .settlement()
                        .appliedFxRate();

        if (occurredAtUnchanged
                && existingFx != null
                && existingFx.baseCurrency()
                .equals(expectedBase)
                && existingFx.quoteCurrency()
                .equals(expectedQuote)) {

            return existingFx;
        }

        FxRate fxRate =
                findFxRate(
                        expectedBase,
                        expectedQuote,
                        newOccurredAt
                );

        return new AppliedFxRate(
                expectedBase,
                expectedQuote,
                fxRate.rate(),
                fxRate.source(),
                fxRate.timestamp()
        );
    }

    private ActivityDetailsResult buildDividendDetails(
            Activity activity,
            ActivityEditDetails requestedDetails
    ) {
        if (activity.isCashDividend()) {

            if (!(requestedDetails
                    instanceof CashDividendDetails details)) {

                throw new InvalidInputException(
                        "La edición no es válida para un DIVIDEND monetario."
                );
            }

            CashActivityDetails current =
                    (CashActivityDetails)
                            activity.getDetails();

            Money amount =
                    new Money(
                            details.amount(),
                            current.amount().currency()
                    );

            return new ActivityDetailsResult(
                    new CashActivityDetails(amount),
                    details.comment()
            );
        }

        if (activity.isUnitsDividend()) {

            if (!(requestedDetails
                    instanceof InKindDividendDetails details)) {

                throw new InvalidInputException(
                        "La edición no es válida para un DIVIDEND en unidades."
                );
            }

            DividendUnitsDetails current =
                    (DividendUnitsDetails)
                            activity.getDetails();

            return new ActivityDetailsResult(
                    new DividendUnitsDetails(
                            details.quantity(),
                            current.referenceUnitPrice()
                    ),
                    details.comment()
            );
        }

        throw new InvalidInputException(
                "El DIVIDEND tiene un formato no reconocido."
        );
    }

    private ActivityDetailsResult buildSplitDetails(
            Activity activity,
            ActivityEditDetails requestedDetails
    ) {
        if (!(requestedDetails instanceof SplitDetails details)) {
            throw new InvalidInputException(
                    "La edición no es válida para SPLIT."
            );
        }

        return new ActivityDetailsResult(
                new SplitActivityDetails(
                        details.ratio()
                ),
                details.comment()
        );
    }

    private ActivityDetailsResult buildExchangeDetails(
            Activity activity,
            ActivityEditDetails requestedDetails,
            Instant newOccurredAt
    ) {
        if (!(requestedDetails
                instanceof ExchangeDetails details)) {

            throw new InvalidInputException(
                    "La edición no es válida para EXCHANGE."
            );
        }

        ExchangeActivityDetails current =
                (ExchangeActivityDetails)
                        activity.getDetails();

        CurrencyCode originCurrency =
                current.origin().currency();

        CurrencyCode destinationCurrency =
                current.destination().currency();

        AppliedFxRate appliedFxRate;

        boolean occurredAtUnchanged =
                newOccurredAt.equals(
                        activity.getOccurredAt()
                );

        if (occurredAtUnchanged) {

            appliedFxRate =
                    current.appliedFxRate();

        } else {

            FxRate fxRate =
                    findFxRate(
                            originCurrency,
                            destinationCurrency,
                            newOccurredAt
                    );

            appliedFxRate =
                    new AppliedFxRate(
                            originCurrency,
                            destinationCurrency,
                            fxRate.rate(),
                            fxRate.source(),
                            fxRate.timestamp()
                    );
        }

        Money origin =
                new Money(
                        details.amountOrigin(),
                        originCurrency
                );

        return new ActivityDetailsResult(
                ExchangeActivityDetails.create(
                        origin,
                        appliedFxRate
                ),
                details.comment()
        );
    }

    private CurrencyCode resolveAssetCurrency(
            UUID userId,
            AssetReference reference
    ) {
        return switch (reference.kind()) {

            case SHARED -> sharedAssets
                    .findById(reference.id())
                    .map(SharedAsset::getCurrency)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "SharedAsset no encontrado"
                            )
                    );

            case USER -> userAssets
                    .findByIdAndUserId(
                            reference.id(),
                            userId
                    )
                    .map(UserAsset::getCurrency)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "UserAsset no encontrado"
                            )
                    );
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

    private void validateCommand(
            EditActivityCommand command
    ) {
        if (command == null) {
            throw new InvalidInputException(
                    "La edición es obligatoria."
            );
        }

        if (command.occurredAt() == null) {
            throw new InvalidInputException(
                    "occurredAt es obligatorio."
            );
        }

        if (command.details() == null) {
            throw new InvalidInputException(
                    "Los datos de edición son obligatorios."
            );
        }
    }

    private String toJson(Activity activity) {
        try {
            return mapper.writeValueAsString(activity);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "No se pudo crear el snapshot de auditoría de la Activity",
                    exception
            );
        }
    }

    private record ActivityDetailsResult(
            com.puntomartinez.millete.investments.domain.model.ActivityDetails details,
            String comment
    ) {
    }
}