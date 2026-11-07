package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AppliedFxRate;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeSettlement;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.ports.in.RecordBuyUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordSellUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityIdempotencyRepository;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

@Service
public class TradeActivityService
        implements RecordBuyUseCase, RecordSellUseCase {

    private static final int AMOUNT_SCALE = 12;

    private final ActivityRepository activities;
    private final ActivityIdempotencyRepository idempotency;
    private final InvestmentPortfolioLockPort portfolioLock;
    private final ActivityOrderingPort ordering;
    private final FxRateRepository fxRates;
    private final SharedAssetRepository sharedAssets;
    private final UserAssetRepository userAssets;
    private final UserCurrencyPort userCurrencies;
    private final PortfolioRebuildService portfolioRebuild;
    private final TimeProvider time;

    public TradeActivityService(
            ActivityRepository activities,
            ActivityIdempotencyRepository idempotency,
            InvestmentPortfolioLockPort portfolioLock,
            ActivityOrderingPort ordering,
            FxRateRepository fxRates,
            SharedAssetRepository sharedAssets,
            UserAssetRepository userAssets,
            UserCurrencyPort userCurrencies,
            PortfolioRebuildService portfolioRebuild,
            TimeProvider time
    ) {
        this.activities = activities;
        this.idempotency = idempotency;
        this.portfolioLock = portfolioLock;
        this.ordering = ordering;
        this.fxRates = fxRates;
        this.sharedAssets = sharedAssets;
        this.userAssets = userAssets;
        this.userCurrencies = userCurrencies;
        this.portfolioRebuild = portfolioRebuild;
        this.time = time;
    }

    @Override
    @Transactional
    public Activity record(
            UUID userId,
            RecordBuyCommand command,
            String idempotencyKey
    ) {
        return recordTrade(
                userId,
                ActivityType.BUY,
                command.occurredAt(),
                command.assetReference(),
                command.quantity(),
                command.unitPrice(),
                command.comment(),
                idempotencyKey
        );
    }

    @Override
    @Transactional
    public Activity record(
            UUID userId,
            RecordSellCommand command,
            String idempotencyKey
    ) {
        return recordTrade(
                userId,
                ActivityType.SELL,
                command.occurredAt(),
                command.assetReference(),
                command.quantity(),
                command.unitPrice(),
                command.comment(),
                idempotencyKey
        );
    }

    private Activity recordTrade(
            UUID userId,
            ActivityType type,
            Instant requestedOccurredAt,
            AssetReference assetReference,
            BigDecimal quantity,
            BigDecimal inputUnitPrice,
            String comment,
            String idempotencyKey
    ) {
        portfolioLock.lock(userId);

        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);

        String fingerprint = fingerprint(
                type,
                requestedOccurredAt,
                assetReference,
                quantity,
                inputUnitPrice,
                comment
        );

        var previous = idempotency
                .findByUserIdAndKey(userId, normalizedKey);

        if (previous.isPresent()) {
            ActivityIdempotencyRepository.IdempotencyEntry entry =
                    previous.get();

            if (!entry.requestFingerprint().equals(fingerprint)) {
                throw new InvalidInputException(
                        "La clave Idempotency-Key ya se usó con otra actividad."
                );
            }

            return activities
                    .findByIdAndUserId(entry.activityId(), userId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "La entrada de idempotencia apunta a una Activity inexistente"
                            )
                    );
        }

        if (assetReference == null) {
            throw new InvalidInputException(
                    "BUY y SELL requieren un AssetReference."
            );
        }

        if (quantity == null || quantity.signum() <= 0) {
            throw new InvalidInputException(
                    "La cantidad debe ser positiva."
            );
        }

        if (inputUnitPrice == null || inputUnitPrice.signum() <= 0) {
            throw new InvalidInputException(
                    "El precio unitario debe ser positivo."
            );
        }

        Instant occurredAt = requestedOccurredAt == null
                ? time.now()
                : requestedOccurredAt;

        CurrencyCode assetCurrency = resolveAssetCurrency(
                userId,
                assetReference
        );

        CurrencyCode localCurrency = userCurrencies
                .currencyAt(userId, occurredAt)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe una moneda local para el instante de la operación."
                        )
                );

        TradeData tradeData = createTradeData(
                quantity,
                inputUnitPrice,
                assetCurrency,
                localCurrency,
                occurredAt
        );

        long orderingKey = ordering.nextOrderingKey(
                userId,
                occurredAt
        );

        Activity activity = Activity.create(
                time,
                userId,
                type,
                assetReference,
                occurredAt,
                orderingKey,
                tradeData.details(),
                comment
        );

        Activity saved = activities.save(activity);

        idempotency.save(
                new ActivityIdempotencyRepository.IdempotencyEntry(
                        userId,
                        normalizedKey,
                        fingerprint,
                        saved.getId(),
                        time.now()
                )
        );

        portfolioRebuild.rebuild(userId);

        return saved;
    }

    private TradeData createTradeData(
            BigDecimal quantity,
            BigDecimal inputUnitPrice,
            CurrencyCode assetCurrency,
            CurrencyCode localCurrency,
            Instant occurredAt
    ) {
        /*
         * El precio se introduce siempre en la divisa del activo.
         * El importe de la operación se calcula en esa misma divisa y,
         * si la divisa local es distinta, se convierte con el tipo de
         * cambio histórico para liquidar contra la caja local.
         */
        Money assetUnitPrice = new Money(
                inputUnitPrice,
                assetCurrency
        );

        Money amountInAssetCurrency = assetUnitPrice.multiply(
                quantity,
                AMOUNT_SCALE
        );

        Money settlementAmount = amountInAssetCurrency;
        AppliedFxRate appliedFxRate = null;

        if (!assetCurrency.equals(localCurrency)) {

            FxRate fx = getFxRate(
                    assetCurrency,
                    localCurrency,
                    occurredAt
            );

            appliedFxRate = toAppliedFxRate(fx);

            Money convertedAmount = amountInAssetCurrency.multiply(
                    fx.rate(),
                    AMOUNT_SCALE
            );

            settlementAmount = new Money(
                    convertedAmount.amount(),
                    localCurrency
            );
        }

        TradeSettlement settlement = new TradeSettlement(
                settlementAmount,
                appliedFxRate
        );

        TradeActivityDetails details =
                new TradeActivityDetails(
                        quantity,
                        assetUnitPrice,
                        settlement
                );

        return new TradeData(
                details
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

    private FxRate getFxRate(
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
                .or(() ->
                        fxRates
                                .findLatestAt(
                                        quoteCurrency,
                                        baseCurrency,
                                        at
                                )
                                .map(FxRate::inverse)
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

    private AppliedFxRate toAppliedFxRate(FxRate fxRate) {
        return new AppliedFxRate(
                fxRate.baseCurrency(),
                fxRate.quoteCurrency(),
                fxRate.rate(),
                fxRate.source(),
                fxRate.timestamp()
        );
    }

    private String normalizeIdempotencyKey(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(
                    "La cabecera Idempotency-Key es obligatoria."
            );
        }

        String normalized = value.trim();

        if (normalized.length() > 128) {
            throw new InvalidInputException(
                    "La cabecera Idempotency-Key no puede superar 128 caracteres."
            );
        }

        return normalized;
    }

    private String fingerprint(
            ActivityType type,
            Instant occurredAt,
            AssetReference assetReference,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String comment
    ) {
        StringBuilder value = new StringBuilder();

        append(value, type);
        append(value, occurredAt);
        append(value, assetReference == null
                ? null
                : assetReference.kind());
        append(value, assetReference == null
                ? null
                : assetReference.id());
        append(value, quantity);
        append(value, unitPrice);
        append(value, comment);

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.toString()
                            .getBytes(StandardCharsets.UTF_8)
            );

            return java.util.HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "No se pudo calcular la huella de la Activity",
                    exception
            );
        }
    }

    private void append(
            StringBuilder builder,
            Object value
    ) {
        if (value == null) {
            builder.append("-1:");
            return;
        }

        String text = value instanceof BigDecimal bigDecimal
                ? bigDecimal.toPlainString()
                : value.toString();

        builder.append(text.length())
                .append(':')
                .append(text);
    }

    private record TradeData(
            TradeActivityDetails details
    ) {
    }
}