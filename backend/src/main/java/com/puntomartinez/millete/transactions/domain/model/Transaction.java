package com.puntomartinez.millete.transactions.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Transaction {

    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private final UUID id;
    private final UUID userId;
    private UUID categoryId;
    private BigDecimal amount;
    private LocalDate date;
    private TransactionType type;
    private String description;
    private final Instant createdAt;
    private Instant modifiedAt;
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
            LocalDate date,
            TransactionType type,
            String description,
            Instant createdAt,
            Instant modifiedAt,
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
            TimeProvider timeProvider,
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            LocalDate date,
            TransactionType type,
            String description
    ) {
        Instant now = timeProvider.now();
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
            LocalDate date,
            TransactionType type,
            String description,
            Instant createdAt,
            Instant modifiedAt,
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
            TimeProvider timeProvider,
            BigDecimal amount,
            LocalDate date,
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
        this.modifiedAt = timeProvider.now();
    }

    public void unassignCategory(TimeProvider timeProvider) {
        if (this.categoryId == null) {
            return;
        }
        this.categoryId = null;
        this.modifiedAt = timeProvider.now();
    }

    public void deactivate(TimeProvider timeProvider) {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.modifiedAt = timeProvider.now();
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new InvalidInputException(
                    "El identificador de la transacción es obligatorio"
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidInputException(
                    "El identificador del usuario es obligatorio"
            );
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException(
                    "La cantidad debe ser mayor que cero."
            );
        }
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new InvalidInputException(
                    "La fecha de la transacción es obligatoria"
            );
        }
    }

    private static void validateType(TransactionType type) {
        if (type == null) {
            throw new InvalidInputException(
                    "El tipo de transacción es obligatorio"
            );
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidInputException(
                    "La descripción de la transacción es obligatoria"
            );
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidInputException(
                    "La descripción no puede superar los "
                            + MAX_DESCRIPTION_LENGTH + " caracteres"
            );
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new InvalidInputException(
                    "La fecha de creación es obligatoria"
            );
        }
    }

    private static void validateModifiedAt(Instant modifiedAt) {
        if (modifiedAt == null) {
            throw new InvalidInputException(
                    "La fecha de modificación es obligatoria"
            );
        }
    }
}
