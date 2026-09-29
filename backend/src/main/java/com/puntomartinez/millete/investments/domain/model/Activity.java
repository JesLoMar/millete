package com.puntomartinez.millete.investments.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;

import java.time.Instant;
import java.util.UUID;

public final class Activity {

    private final UUID id;
    private final UUID userId;
    private final ActivityType type;
    private final AssetReference assetReference;
    private Instant occurredAt;
    private final Instant createdAt;
    private Instant modifiedAt;
    private long orderingKey;
    private ActivityDetails details;
    private String comment;
    private UUID linkedTransactionId;

    private Activity(
            UUID id,
            UUID userId,
            ActivityType type,
            AssetReference assetReference,
            Instant occurredAt,
            Instant createdAt,
            Instant modifiedAt,
            long orderingKey,
            ActivityDetails details,
            String comment,
            UUID linkedTransactionId
    ) {
        validateId(id);
        validateUserId(userId);
        validateType(type);
        validateOccurredAt(occurredAt);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);
        validateOrderingKey(orderingKey);
        validateDetails(details);

        this.id = id;
        this.userId = userId;
        this.type = type;
        this.assetReference = assetReference;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.orderingKey = orderingKey;
        this.details = details;
        this.comment = normalizeComment(comment);
        this.linkedTransactionId = linkedTransactionId;

        validateAssetReference();
        validateLinkedTransaction();
    }

    public static Activity create(
            TimeProvider timeProvider,
            UUID userId,
            ActivityType type,
            AssetReference assetReference,
            Instant occurredAt,
            long orderingKey,
            ActivityDetails details,
            String comment
    ) {
        Instant now = timeProvider.now();

        return new Activity(
                UUID.randomUUID(),
                userId,
                type,
                assetReference,
                occurredAt,
                now,
                now,
                orderingKey,
                details,
                comment,
                null
        );
    }

    public static Activity reconstitute(
            UUID id,
            UUID userId,
            ActivityType type,
            AssetReference assetReference,
            Instant occurredAt,
            Instant createdAt,
            Instant modifiedAt,
            long orderingKey,
            ActivityDetails details,
            String comment,
            UUID linkedTransactionId
    ) {
        return new Activity(
                id,
                userId,
                type,
                assetReference,
                occurredAt,
                createdAt,
                modifiedAt,
                orderingKey,
                details,
                comment,
                linkedTransactionId
        );
    }

    public void edit(
            TimeProvider timeProvider,
            Instant newOccurredAt,
            long newOrderingKey,
            ActivityDetails newDetails,
            String newComment
    ) {
        if (isLinkedCapitalMovement()) {
            throw new IllegalStateException(
                    "Una DEPOSIT o WITHDRAW vinculada a una Transaction no puede editarse"
            );
        }

        validateOccurredAt(newOccurredAt);
        validateOrderingKey(newOrderingKey);
        validateDetails(newDetails);

        if (newOccurredAt.equals(this.occurredAt)) {
            if (newOrderingKey != this.orderingKey) {
                throw new IllegalArgumentException(
                        "Si occurredAt no cambia, orderingKey debe conservarse"
                );
            }
        } else {
            if (newOrderingKey == this.orderingKey) {
                throw new IllegalArgumentException(
                        "Si occurredAt cambia, debe asignarse un nuevo orderingKey"
                );
            }
        }

        validateDetailsForCurrentType(newDetails);

        this.occurredAt = newOccurredAt;
        this.orderingKey = newOrderingKey;
        this.details = newDetails;
        this.comment = normalizeComment(newComment);
        this.modifiedAt = timeProvider.now();

        validateAssetReference();
        validateLinkedTransaction();
    }

    public void attachTransaction(UUID transactionId) {
        if (type != ActivityType.DEPOSIT
                && type != ActivityType.WITHDRAW) {

            throw new IllegalStateException(
                    "Solo DEPOSIT y WITHDRAW pueden vincular una Transaction"
            );
        }

        if (linkedTransactionId != null) {
            throw new IllegalStateException(
                    "La Activity ya tiene una Transaction vinculada"
            );
        }

        if (transactionId == null) {
            throw new IllegalArgumentException(
                    "El identificador de la Transaction es obligatorio"
            );
        }

        linkedTransactionId = transactionId;
    }

    public boolean isLinkedCapitalMovement() {
        return linkedTransactionId != null
                && (
                type == ActivityType.DEPOSIT
                        || type == ActivityType.WITHDRAW
        );
    }

    public boolean isTrade() {
        return type == ActivityType.BUY
                || type == ActivityType.SELL;
    }

    public boolean isCashDividend() {
        return type == ActivityType.DIVIDEND
                && details instanceof CashActivityDetails;
    }

    public boolean isUnitsDividend() {
        return type == ActivityType.DIVIDEND
                && details instanceof DividendUnitsDetails;
    }

    private void validateDetails(
            ActivityDetails details
    ) {
        if (details == null) {
            throw new IllegalArgumentException(
                    "Los datos de la Activity son obligatorios"
            );
        }

        validateDetailsForCurrentType(details);
    }

    private void validateDetailsForCurrentType(
            ActivityDetails details
    ) {
        boolean valid;

        switch (type) {
            case BUY:
            case SELL:
                valid = details instanceof TradeActivityDetails;
                break;

            case DIVIDEND:
                valid = details instanceof CashActivityDetails
                        || details instanceof DividendUnitsDetails;
                break;

            case DEPOSIT:
            case WITHDRAW:
            case OPENING_CASH:
                valid = details instanceof CashActivityDetails;
                break;

            case SPLIT:
                valid = details instanceof SplitActivityDetails;
                break;

            case EXCHANGE:
                valid = details instanceof ExchangeActivityDetails;
                break;

            case OPENING_POSITION:
                valid = details instanceof OpeningPositionDetails;
                break;

            default:
                valid = false;
        }

        if (!valid) {
            throw new IllegalArgumentException(
                    "Los datos no son compatibles con el tipo de Activity "
                            + type
            );
        }
    }

    private void validateAssetReference() {
        boolean requiresAsset =
                type == ActivityType.BUY
                        || type == ActivityType.SELL
                        || type == ActivityType.SPLIT
                        || type == ActivityType.OPENING_POSITION;

        boolean forbidsAsset =
                type == ActivityType.DEPOSIT
                        || type == ActivityType.WITHDRAW
                        || type == ActivityType.OPENING_CASH
                        || type == ActivityType.EXCHANGE;

        if (requiresAsset && assetReference == null) {
            throw new IllegalArgumentException(
                    type + " requiere un AssetReference"
            );
        }

        if (forbidsAsset && assetReference != null) {
            throw new IllegalArgumentException(
                    type + " no puede tener AssetReference"
            );
        }

        if (type == ActivityType.DIVIDEND) {
            if (details instanceof DividendUnitsDetails
                    && assetReference == null) {

                throw new IllegalArgumentException(
                        "Un DIVIDEND en unidades requiere AssetReference"
                );
            }
        }
    }

    private void validateLinkedTransaction() {
        if (linkedTransactionId != null
                && type != ActivityType.DEPOSIT
                && type != ActivityType.WITHDRAW) {

            throw new IllegalArgumentException(
                    "Solo DEPOSIT y WITHDRAW pueden vincular una Transaction"
            );
        }
    }

    private static String normalizeComment(
            String comment
    ) {
        if (comment == null) {
            return null;
        }

        String normalized = comment.trim();

        if (normalized.length() > 500) {
            throw new IllegalArgumentException(
                    "El comentario no puede superar los 500 caracteres"
            );
        }

        return normalized;
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador de la Activity es obligatorio"
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

    private static void validateType(ActivityType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de Activity es obligatorio"
            );
        }
    }

    private static void validateOccurredAt(Instant occurredAt) {
        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de la Activity es obligatoria"
            );
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria"
            );
        }
    }

    private static void validateModifiedAt(Instant modifiedAt) {
        if (modifiedAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de modificación es obligatoria"
            );
        }
    }

    private static void validateOrderingKey(long orderingKey) {
        if (orderingKey < 0) {
            throw new IllegalArgumentException(
                    "orderingKey no puede ser negativo"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public ActivityType getType() {
        return type;
    }

    public AssetReference getAssetReference() {
        return assetReference;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public long getOrderingKey() {
        return orderingKey;
    }

    public ActivityDetails getDetails() {
        return details;
    }

    public String getComment() {
        return comment;
    }

    public UUID getLinkedTransactionId() {
        return linkedTransactionId;
    }
}