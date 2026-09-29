package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.RecordInKindDividendUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordSplitUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityIdempotencyRepository;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityOrderingPort;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.PortfolioProjectionRepository;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
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
public class CorporateActionActivityService
        implements RecordSplitUseCase, RecordInKindDividendUseCase {

    private final ActivityRepository activities;
    private final ActivityIdempotencyRepository idempotency;
    private final InvestmentPortfolioLockPort portfolioLock;
    private final ActivityOrderingPort ordering;
    private final SharedAssetRepository sharedAssets;
    private final UserAssetRepository userAssets;
    private final AssetPriceRepository assetPrices;
    private final UserAssetPriceRepository userAssetPrices;
    private final PortfolioRebuildService portfolioRebuild;
    private final TimeProvider time;

    public CorporateActionActivityService(
            ActivityRepository activities,
            ActivityIdempotencyRepository idempotency,
            InvestmentPortfolioLockPort portfolioLock,
            ActivityOrderingPort ordering,
            SharedAssetRepository sharedAssets,
            UserAssetRepository userAssets,
            AssetPriceRepository assetPrices,
            UserAssetPriceRepository userAssetPrices,
            PortfolioRebuildService portfolioRebuild,
            TimeProvider time
    ) {
        this.activities = activities;
        this.idempotency = idempotency;
        this.portfolioLock = portfolioLock;
        this.ordering = ordering;
        this.sharedAssets = sharedAssets;
        this.userAssets = userAssets;
        this.assetPrices = assetPrices;
        this.userAssetPrices = userAssetPrices;
        this.portfolioRebuild = portfolioRebuild;
        this.time = time;
    }

    @Override
    @Transactional
    public Activity record(
            UUID userId,
            RecordSplitCommand command,
            String idempotencyKey
    ) {
        portfolioLock.lock(userId);

        String normalizedKey =
                normalizeIdempotencyKey(idempotencyKey);

        String fingerprint = fingerprint(
                ActivityType.SPLIT,
                command.occurredAt(),
                command.assetReference(),
                command.ratio(),
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

        validateAssetReference(
                userId,
                command.assetReference()
        );

        Instant occurredAt = command.occurredAt() == null
                ? time.now()
                : command.occurredAt();

        long orderingKey =
                ordering.nextOrderingKey(
                        userId,
                        occurredAt
                );

        Activity activity = Activity.create(
                time,
                userId,
                ActivityType.SPLIT,
                command.assetReference(),
                occurredAt,
                orderingKey,
                new SplitActivityDetails(
                        command.ratio()
                ),
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
            RecordInKindDividendCommand command,
            String idempotencyKey
    ) {
        portfolioLock.lock(userId);

        String normalizedKey =
                normalizeIdempotencyKey(idempotencyKey);

        String fingerprint = fingerprint(
                ActivityType.DIVIDEND,
                command.occurredAt(),
                command.assetReference(),
                command.quantity(),
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

        validateAssetReference(
                userId,
                command.assetReference()
        );

        Instant occurredAt = command.occurredAt() == null
                ? time.now()
                : command.occurredAt();

        Money referenceUnitPrice =
                findReferenceUnitPrice(
                        userId,
                        command.assetReference(),
                        occurredAt
                );

        long orderingKey =
                ordering.nextOrderingKey(
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
                new DividendUnitsDetails(
                        command.quantity(),
                        referenceUnitPrice
                ),
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

    private Money findReferenceUnitPrice(
            UUID userId,
            AssetReference reference,
            Instant occurredAt
    ) {
        return switch (reference.kind()) {

            case SHARED -> {
                SharedAsset asset =
                        sharedAssets.findById(reference.id())
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "SharedAsset no encontrado"
                                        )
                                );

                AssetPrice price =
                        assetPrices.findLatestAt(
                                        asset.getId(),
                                        occurredAt
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "No existe un precio de mercado disponible para el momento del dividendo"
                                        )
                                );

                BigDecimal valuationPrice =
                        price.valuationPrice();

                if (valuationPrice == null) {
                    throw new IllegalStateException(
                            "El precio de mercado no contiene un valor de valoración"
                    );
                }

                if (!price.currency().equals(
                        asset.getCurrency()
                )) {
                    throw new IllegalStateException(
                            "La moneda del precio no coincide con la moneda del SharedAsset"
                    );
                }

                yield new Money(
                        valuationPrice,
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

                UserAssetPrice price =
                        userAssetPrices.findLatestAt(
                                        asset.getId(),
                                        occurredAt
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "No existe una valoración disponible para el momento del dividendo"
                                        )
                                );

                if (!price.getUnitPrice().currency().equals(
                        asset.getCurrency()
                )) {
                    throw new IllegalStateException(
                            "La moneda de la valoración no coincide con la moneda del UserAsset"
                    );
                }

                yield price.getUnitPrice();
            }
        };
    }

    private void validateAssetReference(
            UUID userId,
            AssetReference reference
    ) {
        if (reference == null) {
            throw new InvalidInputException(
                    "La Activity requiere un AssetReference."
            );
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
                .findByUserIdAndKey(
                        userId,
                        key
                )
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
                                    )
                            );
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

    private String normalizeIdempotencyKey(
            String value
    ) {
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
            Object... values
    ) {
        StringBuilder builder =
                new StringBuilder();

        for (Object value : values) {

            if (value == null) {
                builder.append("-1:");
                continue;
            }

            String text =
                    value instanceof BigDecimal bigDecimal
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