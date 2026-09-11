package com.puntomartinez.millete.plannedtransactions.domain.model;

import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PlannedTransaction {

    public enum FrequencyType {
        DAYS,
        WEEKS,
        MONTHS,
        YEARS
    }

    private final UUID id;
    private final UUID userId;

    private UUID categoryId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private FrequencyType frequencyType;
    private Integer frequencyInterval;
    private LocalDate startDate;
    private LocalDate endDate;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;
    private LocalDate lastExecutedDate;

    private PlannedTransaction(
            UUID id,
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            TransactionType type,
            String description,
            FrequencyType frequencyType,
            Integer frequencyInterval,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active,
            LocalDate lastExecutedDate
    ) {
        validateId(id);
        validateUserId(userId);
        validateAmount(amount);
        validateType(type);
        validateDescription(description);
        validateFrequencyType(frequencyType);
        validateFrequencyInterval(frequencyInterval);
        validateStartDate(startDate);
        validateEndDate(startDate, endDate);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.frequencyType = frequencyType;
        this.frequencyInterval = frequencyInterval;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
        this.lastExecutedDate = lastExecutedDate;
    }

    public static PlannedTransaction create(
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            TransactionType type,
            String description,
            FrequencyType frequencyType,
            Integer frequencyInterval,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new PlannedTransaction(
                UUID.randomUUID(),
                userId,
                categoryId,
                amount,
                type,
                description,
                frequencyType,
                frequencyInterval,
                startDate,
                endDate,
                now,
                now,
                true,
                null
        );
    }

    public static PlannedTransaction reconstitute(
            UUID id,
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            TransactionType type,
            String description,
            FrequencyType frequencyType,
            Integer frequencyInterval,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active,
            LocalDate lastExecutedDate
    ) {
        return new PlannedTransaction(
                id,
                userId,
                categoryId,
                amount,
                type,
                description,
                frequencyType,
                frequencyInterval,
                startDate,
                endDate,
                createdAt,
                modifiedAt,
                active,
                lastExecutedDate
        );
    }

    public void updateDetails(
            UUID categoryId,
            BigDecimal amount,
            TransactionType type,
            String description,
            FrequencyType frequencyType,
            Integer frequencyInterval,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateAmount(amount);
        validateType(type);
        validateDescription(description);
        validateFrequencyType(frequencyType);
        validateFrequencyInterval(frequencyInterval);
        validateStartDate(startDate);
        validateEndDate(startDate, endDate);

        boolean recurrenceChanged =
                !this.startDate.equals(startDate)
                        || this.frequencyType != frequencyType
                        || !this.frequencyInterval.equals(frequencyInterval);

        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.frequencyType = frequencyType;
        this.frequencyInterval = frequencyInterval;
        this.startDate = startDate;
        this.endDate = endDate;

        if (recurrenceChanged) {
            this.lastExecutedDate = null;
        }

        this.modifiedAt = LocalDateTime.now();
    }

    public void markAsExecuted(LocalDate executionDate) {
        if (executionDate == null) {
            throw new IllegalArgumentException(
                    "La fecha de ejecución es obligatoria."
            );
        }

        if (executionDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException(
                    "La fecha de ejecución no puede ser anterior a la fecha de inicio."
            );
        }

        if (this.endDate != null && executionDate.isAfter(this.endDate)) {
            throw new IllegalArgumentException(
                    "La fecha de ejecución no puede ser posterior a la fecha de fin."
            );
        }

        this.lastExecutedDate = executionDate;
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
                    "El identificador de la transacción planificada es obligatorio."
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El identificador del usuario es obligatorio."
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

    private static void validateType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de transacción es obligatorio."
            );
        }
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > 50) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar los 50 caracteres."
            );
        }
    }

    private static void validateFrequencyType(FrequencyType frequencyType) {
        if (frequencyType == null) {
            throw new IllegalArgumentException(
                    "El tipo de frecuencia es obligatorio."
            );
        }
    }

    private static void validateFrequencyInterval(Integer frequencyInterval) {
        if (frequencyInterval == null || frequencyInterval <= 0) {
            throw new IllegalArgumentException(
                    "El intervalo de frecuencia debe ser mayor que cero."
            );
        }
    }

    private static void validateStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio es obligatoria."
            );
        }
    }

    private static void validateEndDate(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio."
            );
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria."
            );
        }
    }

    private static void validateModifiedAt(LocalDateTime modifiedAt) {
        if (modifiedAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de modificación es obligatoria."
            );
        }
    }
}