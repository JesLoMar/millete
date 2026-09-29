package com.puntomartinez.millete.investments.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;

import java.time.Instant;
import java.util.UUID;

public final class UserAssetPrice {

    private final UUID id;
    private final UUID userAssetId;
    private final Money unitPrice;
    private final Instant timestamp;

    private UserAssetPrice(
            UUID id,
            UUID userAssetId,
            Money unitPrice,
            Instant timestamp
    ) {
        validateId(id);
        validateUserAssetId(userAssetId);
        validateUnitPrice(unitPrice);
        validateTimestamp(timestamp);

        this.id = id;
        this.userAssetId = userAssetId;
        this.unitPrice = unitPrice;
        this.timestamp = timestamp;
    }

    public static UserAssetPrice create(
            TimeProvider timeProvider,
            UUID userAssetId,
            Money unitPrice
    ) {
        return new UserAssetPrice(
                UUID.randomUUID(),
                userAssetId,
                unitPrice,
                timeProvider.now()
        );
    }

    public static UserAssetPrice reconstitute(
            UUID id,
            UUID userAssetId,
            Money unitPrice,
            Instant timestamp
    ) {
        return new UserAssetPrice(
                id,
                userAssetId,
                unitPrice,
                timestamp
        );
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del precio es obligatorio"
            );
        }
    }

    private static void validateUserAssetId(
            UUID userAssetId
    ) {
        if (userAssetId == null) {
            throw new IllegalArgumentException(
                    "El UserAsset es obligatorio"
            );
        }
    }

    private static void validateUnitPrice(
            Money unitPrice
    ) {
        if (unitPrice == null) {
            throw new IllegalArgumentException(
                    "El precio unitario es obligatorio"
            );
        }

        if (unitPrice.amount().signum() < 0) {
            throw new IllegalArgumentException(
                    "El precio unitario no puede ser negativo"
            );
        }
    }

    private static void validateTimestamp(
            Instant timestamp
    ) {
        if (timestamp == null) {
            throw new IllegalArgumentException(
                    "La fecha del precio es obligatoria"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserAssetId() {
        return userAssetId;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}