package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AppliedFxRate;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.ports.in.RecordCashDividendUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordDepositUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordExchangeUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordWithdrawalUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityIdempotencyRepository;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityOrderingPort;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.TransactionIntegrationPort;
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
public class CashActivityService implements
        RecordDepositUseCase,
        RecordWithdrawalUseCase,
        RecordCashDividendUseCase,
        RecordExchangeUseCase {

    private static final int MONEY_SCALE = 12;

    private final ActivityRepository activities;
    private final ActivityIdempotencyRepository idempotency;
    private final InvestmentPortfolioLockPort portfolioLock;
    private final ActivityOrderingPort ordering;
    private final FxRateRepository fxRates;
    private final SharedAssetRepository sharedAssets;
    private final UserAssetRepository userAssets;
    private final UserCurrencyPort userCurrencies;
    private final TransactionIntegrationPort transactions;
    private final PortfolioRebuildService portfolioRebuild;
    private final TimeProvider time;

    public CashActivityService(
            ActivityRepository activities,
            ActivityIdempotencyRepository idempotency,
            InvestmentPortfolioLockPort portfolioLock,
            ActivityOrderingPort ordering,
            FxRateRepository fxRates,
            SharedAssetRepository sharedAssets,
            UserAssetRepository userAssets,
            UserCurrencyPort userCurrencies,
            TransactionIntegrationPort transactions,
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
        this.transactions = transactions;
        this.portfolioRebuild = portfolioRebuild;
        this.time = time;
    }

    @Override
    @Transactional
    public Activity record(
            UUID userId,
            RecordDepositCommand command,
            String idempotencyKey
    ) {
        return recordCapitalMovement(
                userId,
                ActivityType.DEPOSIT,
                command.occurredAt(),
                command.amount(),
                command.currency(),
                command.comment(),
                idempotencyKey
        );
    }

    @Override
    @Transactional
    public Activity record(
            UUID userId,
            RecordWithdrawalCommand command,
            String idempotencyKey
    ) {
        return recordCapitalMovement(
                userId,
                ActivityType.WITHDRAW,
                command.occurredAt(),
                command.amount(),
                command.currency(),
                command.comment(),
                idempotencyKey
        );
    }

    @Override
    @Transactional
    public Activity record(
            UUID userId,
            RecordCashDividendCommand command,
            String idempotencyKey
    ) {
        portfolioLock.lock(userId);

        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);

        String fingerprint = fingerprint(
                ActivityType.DIVIDEND,
                command.occurredAt(),
                command.assetReference(),
                command.amount(),
                command.currency(),
                command.comment
        );

        Activity previous = findPreviousActivity(
                userId,
                normalizedKey,
                fingerprint
        );

        if (previous != null) {
            return previous;
        }

        validateAssetReference(
                userId,
                command.assetReference()
        );

        Instant occurredAt = command.occurredAt() == null
                ? time.now()
                : command.occurredAt();

        Money amount = Money.of(
                command.amount(),
                command.currency()
        );

        long orderingKey = ordering.nextOrderingKey(
                userId,
                occurredAt
        );

        Activity activity = Activity.create(
                time,
                userId,
                ActivityType.DIVIDEND,
                command.assetReference(),
                occurredAt,
                orderingKey,
                new CashActivityDetails(amount),
                command.comment()
        );

        Activity saved = activities.save(activity);

        saveIdempotency(
                userId,
                normalizedKey,
                fingerprint,
                saved.getId()
        );

        portfolioRebuild.rebuild(userId);

        return saved;
    }

    @Override
    @Transactional
    public Activity record(
            UUID userId,
            RecordExchangeCommand command,
            String idempotencyKey
    ) {
        portfolioLock.lock(userId);

        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);

        String fingerprint = fingerprint(
                ActivityType.EXCHANGE,
                command.occurredAt(),
                command.amountOrigin(),
                command.currencyOrigin(),
                command.currencyDestination(),
                command.comment()
        );

        Activity previous = findPreviousActivity(
                userId,
                normalizedKey,
                fingerprint
        );

        if (previous != null) {
            return previous;
        }

        Instant occurredAt = command.occurredAt() == null
                ? time.now()
                : command.occurredAt();

        CurrencyCode originCurrency =
                CurrencyCode.of(command.currencyOrigin());

        CurrencyCode destinationCurrency =
                CurrencyCode.of(command.currencyDestination());

        if (originCurrency.equals(destinationCurrency)) {
            throw new InvalidInputException(
                    "Las monedas de una conversión deben ser distintas."
            );
        }

        Money origin = new Money(
                command.amountOrigin(),
                originCurrency
        );

        FxRate fxRate = findFxRate(
                originCurrency,
                destinationCurrency,
                occurredAt
        );

        AppliedFxRate appliedFxRate =
                toAppliedFxRate(
                        fxRate,
                        originCurrency,
                        destinationCurrency
                );

        ExchangeActivityDetails details =
                ExchangeActivityDetails.create(
                        origin,
                        appliedFxRate
                );

        long orderingKey = ordering.nextOrderingKey(
                userId,
                occurredAt
        );

        Activity activity = Activity.create(
                time,
                userId,
                ActivityType.EXCHANGE,
                null,
                occurredAt,
                orderingKey,
                details,
                command.comment()
        );

        Activity saved = activities.save(activity);

        saveIdempotency(
                userId,
                normalizedKey,
                fingerprint,
                saved.getId()
        );

        portfolioRebuild.rebuild(userId);

        return saved;
    }

    private Activity recordCapitalMovement(
            UUID userId,
            ActivityType type,
            Instant requestedOccurredAt,
            BigDecimal amountValue,
            String currencyValue,
            String comment,
            String idempotencyKey
    ) {
        portfolioLock.lock(userId);

        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);

        String fingerprint = fingerprint(
                type,
                requestedOccurredAt,
                amountValue,
                currencyValue,
                comment
        );

        Activity previous = findPreviousActivity(
                userId,
                normalizedKey,
                fingerprint
        );

        if (previous != null) {
            return previous;
        }

        Instant occurredAt = requestedOccurredAt == null
                ? time.now()
                : requestedOccurredAt;

        Money amount = Money.of(
                amountValue,
                currencyValue
        );

        long orderingKey = ordering.nextOrderingKey(
                userId,
                occurredAt
        );

        Activity activity = Activity.create(
                time,
                userId,
                type,
                null,
                occurredAt,
                orderingKey,
                new CashActivityDetails(amount),
                comment
        );

        Activity saved = activities.save(activity);

        Money localAmount =
                convertToLocalCurrency(
                        userId,
                        amount,
                        occurredAt
                );

        TransactionIntegrationPort.TransferDirection direction =
                type == ActivityType.DEPOSIT
                        ? TransactionIntegrationPort.TransferDirection.TRANSFER_OUT
                        : TransactionIntegrationPort.TransferDirection.TRANSFER_IN;

        UUID transactionId =
                transactions.createInvestmentTransfer(
                        userId,
                        saved.getId(),
                        direction,
                        localAmount,
                        occurredAt,
                        type.name() + " investment transfer"
                );

        saved.attachTransaction(transactionId);

        saved = activities.save(saved);

        saveIdempotency(
                userId,
                normalizedKey,
                fingerprint,
                saved.getId()
        );

        portfolioRebuild.rebuild(userId);

        return saved;
    }

    private Money convertToLocalCurrency(
            UUID userId,
            Money amount,
            Instant occurredAt
    ) {
        CurrencyCode localCurrency =
                userCurrencies
                        .currencyAt(userId, occurredAt)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe moneda local para el instante de la operación."
                                )
                        );

        if (amount.currency().equals(localCurrency)) {
            return amount;
        }

        FxRate fxRate = findFxRate(
                amount.currency(),
                localCurrency,
                occurredAt
        );

        BigDecimal localAmount =
                amount.amount()
                        .multiply(fxRate.rate())
                        .setScale(
                                MONEY_SCALE,
                                java.math.RoundingMode.HALF_UP
                        );

        return new Money(
                localAmount,
                localCurrency
        );
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
                .orElseGet(() ->
                        fxRates
                                .findLatestAt(
                                        quoteCurrency,
                                        baseCurrency,
                                        at
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "No existe un tipo de cambio histórico disponible entre "
                                                        + baseCurrency.value()
                                                        + " y "
                                                        + quoteCurrency.value()
                                        )
                                )
                );
    }

    private AppliedFxRate toAppliedFxRate(
            FxRate fxRate,
            CurrencyCode requestedBase,
            CurrencyCode requestedQuote
    ) {
        if (fxRate.baseCurrency().equals(requestedBase)
                && fxRate.quoteCurrency().equals(requestedQuote)) {
            return new AppliedFxRate(
                    fxRate.baseCurrency(),
                    fxRate.quoteCurrency(),
                    fxRate.rate(),
                    fxRate.source(),
                    fxRate.timestamp()
            );
        }

        BigDecimal inverseRate =
                BigDecimal.ONE.divide(
                        fxRate.rate(),
                        MONEY_SCALE,
                        java.math.RoundingMode.HALF_UP
                );

        return new AppliedFxRate(
                requestedBase,
                requestedQuote,
                inverseRate,
                "INVERSE:" + fxRate.source(),
                fxRate.timestamp()
        );
    }

    private void validateAssetReference(
            UUID userId,
            AssetReference reference
    ) {
        if (reference == null) {
            return;
        }

        switch (reference.kind()) {
            case SHARED -> sharedAssets
                    .findById(reference.id())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "SharedAsset no encontrado"
                            ));

            case USER -> userAssets
                    .findByIdAndUserId(
                            reference.id(),
                            userId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "UserAsset no encontrado"
                            ));
        }
    }

    private Activity findPreviousActivity(
            UUID userId,
            String key,
            String fingerprint
    ) {
        return idempotency
                .findByUserIdAndKey(userId, key)
                .map(entry -> {
                    if (!entry.requestFingerprint()
                            .equals(fingerprint)) {
                        throw new InvalidInputException(
                                "La clave Idempotency-Key ya se usó con otra actividad."
                        );
                    }

                    return activities
                            .findByIdAndUserId(
                                    entry.activityId(),
                                    userId
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "La entrada de idempotencia apunta a una Activity inexistente"
                                    ));
                })
                .orElse(null);
    }

    private void saveIdempotency(
            UUID userId,
            String key,
            String fingerprint,
            UUID activityId
    ) {
        idempotency.save(
                new ActivityIdempotencyRepository.IdempotencyEntry(
                        userId,
                        key,
                        fingerprint,
                        activityId,
                        time.now()
                )
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

    private String fingerprint(Object... values) {
        StringBuilder builder = new StringBuilder();

        for (Object value : values) {
            if (value == null) {
                builder.append("-1:");
                continue;
            }

            String text = value instanceof BigDecimal bigDecimal
                    ? bigDecimal.toPlainString()
                    : value.toString();

            builder
                    .append(text.length())
                    .append(':')
                    .append(text);
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return java.util.HexFormat
                    .of()
                    .formatHex(
                            digest.digest(
                                    builder
                                            .toString()
                                            .getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                            )
                    );

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "No se pudo calcular la huella de la Activity",
                    exception
            );
        }
    }
}