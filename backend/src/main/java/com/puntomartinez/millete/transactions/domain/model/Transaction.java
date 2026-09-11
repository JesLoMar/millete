package com.puntomartinez.millete.transactions.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Transaction {

    private final UUID id;
    private final UUID userId;
    private UUID categoryId;
    private BigDecimal amount;
    private LocalDateTime date;
    private TransactionType type;
    private String description;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    public enum TransactionType {
        INCOME,
        EXPENSE
    }

    private Transaction(
            UUID id,
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            LocalDateTime date,
            TransactionType type,
            String description,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active
    ) {
        validateId(id);
        validateUserId(userId);
        validateAmount(amount);
        validateDate(date);
        validateType(type);
        validateDescription(description);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    public static Transaction create(
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            LocalDateTime date,
            TransactionType type,
            String description
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new Transaction(
                UUID.randomUUID(),
                userId,
                categoryId,
                amount,
                date,
                type,
                description,
                now,
                now,
                true
        );
    }

    public static Transaction reconstitute(
            UUID id,
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            LocalDateTime date,
            TransactionType type,
            String description,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active
    ) {
        return new Transaction(
                id,
                userId,
                categoryId,
                amount,
                date,
                type,
                description,
                createdAt,
                modifiedAt,
                active
        );
    }

    public void updateDetails(
            BigDecimal amount,
            LocalDateTime date,
            TransactionType type,
            String description,
            UUID categoryId
    ) {
        validateAmount(amount);
        validateDate(date);
        validateType(type);
        validateDescription(description);

        this.amount = amount;
        this.date = date;
        this.type = type;
        this.description = description;
        this.categoryId = categoryId;
        this.modifiedAt = LocalDateTime.now();
    }

    public void unassignCategory() {
        if (this.categoryId == null) {
            return;
        }

        this.categoryId = null;
        this.modifiedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.active) {
            return;
        }

        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador de la transacción es obligatorio"
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

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }
    }

    private static void validateDate(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException(
                    "La fecha de la transacción es obligatoria"
            );
        }
    }

    private static void validateType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de transacción es obligatorio"
            );
        }
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > 50) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar los 50 caracteres"
            );
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria"
            );
        }
    }

    private static void validateModifiedAt(LocalDateTime modifiedAt) {
        if (modifiedAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de modificación es obligatoria"
            );
        }
    }
}