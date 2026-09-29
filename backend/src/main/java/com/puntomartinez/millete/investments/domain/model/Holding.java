package com.puntomartinez.millete.investments.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Holding {

    private final UUID id;
    private final UUID userId;
    private final AssetReference assetReference;
    private final Instant snapshotAt;
    private final BigDecimal quantity;
    private final Money acquisitionCost;
    private HoldingStatus status;
    private final Instant createdAt;
    private Instant supersededAt;

    private Holding(
            UUID id,
            UUID userId,
            AssetReference assetReference,
            Instant snapshotAt,
            BigDecimal quantity,
            Money acquisitionCost,
            HoldingStatus status,
            Instant createdAt,
            Instant supersededAt
    ) {
        validateId(id);
        validateUserId(userId);
        validateAssetReference(assetReference);
        validateSnapshotAt(snapshotAt);
        validateQuantity(quantity);
        validateAcquisitionCost(acquisitionCost);
        validateStatus(status);
        validateCreatedAt(createdAt);
        validateState(status, supersededAt);

        this.id = id;
        this.userId = userId;
        this.assetReference = assetReference;
        this.snapshotAt = snapshotAt;
        this.quantity = quantity;
        this.acquisitionCost = acquisitionCost;
        this.status = status;
        this.createdAt = createdAt;
        this.supersededAt = supersededAt;
    }

    public static Holding create(
            TimeProvider timeProvider,
            UUID userId,
            AssetReference assetReference,
            Instant snapshotAt,
            BigDecimal quantity,
            Money acquisitionCost
    ) {
        Instant now = timeProvider.now();

        if (snapshotAt.isAfter(now)) {
            throw new IllegalArgumentException(
                    "snapshotAt no puede estar en el futuro"
            );
        }

        return new Holding(
                UUID.randomUUID(),
                userId,
                assetReference,
                snapshotAt,
                quantity,
                acquisitionCost,
                HoldingStatus.ACTIVE,
                now,
                null
        );
    }

    public static Holding reconstitute(
            UUID id,
            UUID userId,
            AssetReference assetReference,
            Instant snapshotAt,
            BigDecimal quantity,
            Money acquisitionCost,
            HoldingStatus status,
            Instant createdAt,
            Instant supersededAt
    ) {
        return new Holding(
                id,
                userId,
                assetReference,
                snapshotAt,
                quantity,
                acquisitionCost,
                status,
                createdAt,
                supersededAt
        );
    }

    public void markSuperseded(
            TimeProvider timeProvider
    ) {
        if (status != HoldingStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Solo un Holding ACTIVE puede pasar a SUPERSEDED"
            );
        }

        status = HoldingStatus.SUPERSEDED;
        supersededAt = timeProvider.now();
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del Holding es obligatorio"
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El identificador del usuario es obligatorio"
            );
        }
    }

    private static void validateAssetReference(
            AssetReference assetReference
    ) {
        if (assetReference == null) {
            throw new IllegalArgumentException(
                    "El AssetReference es obligatorio"
            );
        }
    }

    private static void validateSnapshotAt(
            Instant snapshotAt
    ) {
        if (snapshotAt == null) {
            throw new IllegalArgumentException(
                    "snapshotAt es obligatorio"
            );
        }
    }

    private static void validateQuantity(
            BigDecimal quantity
    ) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad del Holding debe ser positiva"
            );
        }
    }

    private static void validateAcquisitionCost(
            Money acquisitionCost
    ) {
        if (acquisitionCost == null) {
            throw new IllegalArgumentException(
                    "El coste de adquisición es obligatorio"
            );
        }

        if (acquisitionCost.amount().signum() < 0) {
            throw new IllegalArgumentException(
                    "El coste de adquisición no puede ser negativo"
            );
        }
    }

    private static void validateStatus(
            HoldingStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "El estado del Holding es obligatorio"
            );
        }
    }

    private static void validateCreatedAt(
            Instant createdAt
    ) {
        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria"
            );
        }
    }

    private static void validateState(
            HoldingStatus status,
            Instant supersededAt
    ) {
        if (status == HoldingStatus.ACTIVE
                && supersededAt != null) {

            throw new IllegalArgumentException(
                    "Un Holding ACTIVE no puede tener supersededAt"
            );
        }

        if (status == HoldingStatus.SUPERSEDED
                && supersededAt == null) {

            throw new IllegalArgumentException(
                    "Un Holding SUPERSEDED debe tener supersededAt"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public AssetReference getAssetReference() {
        return assetReference;
    }

    public Instant getSnapshotAt() {
        return snapshotAt;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Money getAcquisitionCost() {
        return acquisitionCost;
    }

    public HoldingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSupersededAt() {
        return supersededAt;
    }
}