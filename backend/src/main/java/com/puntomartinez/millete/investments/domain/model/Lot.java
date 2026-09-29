package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Lot {

    public static final int COST_BASIS_SCALE = 12;
    public static final int UNIT_COST_SCALE = 18;

    private final UUID id;
    private final UUID userId;
    private final AssetReference assetReference;
    private final LotSource source;
    private final Instant acquiredAt;
    private final long acquisitionOrder;

    private BigDecimal originalQuantity;
    private BigDecimal remainingQuantity;
    private final Money totalCost;

    private Lot(
            UUID id,
            UUID userId,
            AssetReference assetReference,
            LotSource source,
            Instant acquiredAt,
            long acquisitionOrder,
            BigDecimal originalQuantity,
            BigDecimal remainingQuantity,
            Money totalCost
    ) {
        validateId(id);
        validateUserId(userId);
        validateAssetReference(assetReference);
        validateSource(source);
        validateAcquiredAt(acquiredAt);
        validateAcquisitionOrder(acquisitionOrder);
        validateOriginalQuantity(originalQuantity);
        validateRemainingQuantity(
                originalQuantity,
                remainingQuantity
        );
        validateTotalCost(totalCost);

        this.id = id;
        this.userId = userId;
        this.assetReference = assetReference;
        this.source = source;
        this.acquiredAt = acquiredAt;
        this.acquisitionOrder = acquisitionOrder;
        this.originalQuantity = originalQuantity;
        this.remainingQuantity = remainingQuantity;
        this.totalCost = totalCost;
    }

    public static Lot fromActivity(
            UUID id,
            UUID userId,
            AssetReference assetReference,
            UUID activityId,
            Instant acquiredAt,
            long acquisitionOrder,
            BigDecimal quantity,
            Money totalCost
    ) {
        return new Lot(
                id,
                userId,
                assetReference,
                LotSource.activity(activityId),
                acquiredAt,
                acquisitionOrder,
                quantity,
                quantity,
                totalCost
        );
    }

    public static Lot fromHolding(
            UUID id,
            UUID userId,
            AssetReference assetReference,
            UUID holdingId,
            Instant acquiredAt,
            long acquisitionOrder,
            BigDecimal quantity,
            Money totalCost
    ) {
        return new Lot(
                id,
                userId,
                assetReference,
                LotSource.holding(holdingId),
                acquiredAt,
                acquisitionOrder,
                quantity,
                quantity,
                totalCost
        );
    }

    public static Lot reconstitute(
            UUID id,
            UUID userId,
            AssetReference assetReference,
            LotSource source,
            Instant acquiredAt,
            long acquisitionOrder,
            BigDecimal originalQuantity,
            BigDecimal remainingQuantity,
            Money totalCost
    ) {
        return new Lot(
                id,
                userId,
                assetReference,
                source,
                acquiredAt,
                acquisitionOrder,
                originalQuantity,
                remainingQuantity,
                totalCost
        );
    }

    public Money consume(
            BigDecimal quantity
    ) {
        if (quantity == null
                || quantity.signum() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad consumida debe ser positiva"
            );
        }

        if (quantity.compareTo(
                remainingQuantity
        ) > 0) {

            throw new IllegalArgumentException(
                    "El Lot no dispone de suficientes unidades"
            );
        }

        Money costBasis =
                getUnitCost().multiply(
                        quantity,
                        COST_BASIS_SCALE
                );

        remainingQuantity =
                remainingQuantity.subtract(quantity);

        return costBasis;
    }

    public void split(
            BigDecimal ratio
    ) {
        if (ratio == null || ratio.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El ratio del split debe ser positivo"
            );
        }

        if (remainingQuantity.signum() == 0) {
            throw new IllegalStateException(
                    "Un Lot cerrado no puede recibir un split"
            );
        }

        originalQuantity =
                originalQuantity.multiply(ratio);

        remainingQuantity =
                remainingQuantity.multiply(ratio);

        /*
         * totalCost permanece igual.
         * El split modifica la cantidad de unidades,
         * no el coste total del Lot.
         */
    }

    public Money getUnitCost() {
        return totalCost.divide(
                originalQuantity,
                UNIT_COST_SCALE
        );
    }

    public boolean isSynthetic() {
        return source.type() == LotSourceType.HOLDING;
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del Lot es obligatorio"
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

    private static void validateSource(
            LotSource source
    ) {
        if (source == null) {
            throw new IllegalArgumentException(
                    "El origen del Lot es obligatorio"
            );
        }
    }

    private static void validateAcquiredAt(
            Instant acquiredAt
    ) {
        if (acquiredAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de adquisición es obligatoria"
            );
        }
    }

    private static void validateAcquisitionOrder(
            long acquisitionOrder
    ) {
        if (acquisitionOrder < 0) {
            throw new IllegalArgumentException(
                    "acquisitionOrder no puede ser negativo"
            );
        }
    }

    private static void validateOriginalQuantity(
            BigDecimal originalQuantity
    ) {
        if (originalQuantity == null
                || originalQuantity.signum() <= 0) {

            throw new IllegalArgumentException(
                    "originalQuantity debe ser positiva"
            );
        }
    }

    private static void validateRemainingQuantity(
            BigDecimal originalQuantity,
            BigDecimal remainingQuantity
    ) {
        if (remainingQuantity == null
                || remainingQuantity.signum() < 0
                || remainingQuantity.compareTo(
                originalQuantity
        ) > 0) {

            throw new IllegalArgumentException(
                    "remainingQuantity está fuera de los límites del Lot"
            );
        }
    }

    private static void validateTotalCost(
            Money totalCost
    ) {
        if (totalCost == null) {
            throw new IllegalArgumentException(
                    "El coste total es obligatorio"
            );
        }

        if (totalCost.amount().signum() < 0) {
            throw new IllegalArgumentException(
                    "El coste total no puede ser negativo"
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

    public LotSource getSource() {
        return source;
    }

    public Instant getAcquiredAt() {
        return acquiredAt;
    }

    public long getAcquisitionOrder() {
        return acquisitionOrder;
    }

    public BigDecimal getOriginalQuantity() {
        return originalQuantity;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public Money getTotalCost() {
        return totalCost;
    }
}