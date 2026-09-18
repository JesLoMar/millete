package com.puntomartinez.millete.plannedtransactions.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PlannedTransaction {

    public static final int MAX_CONSECUTIVE_FAILURES = 3;
    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private FrequencyType frequencyType;
    private Integer frequencyInterval;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;
    private LocalDate lastExecutedDate;
    private int failureCount;

    public enum FrequencyType {
        DAYS,
        WEEKS,
        MONTHS,
        YEARS
    }

    private PlannedTransaction() {
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
        validateUserId(userId);
        validateAmount(amount);
        validateType(type);
        validateDescription(description);
        validateFrequencyType(frequencyType);
        validateFrequencyInterval(frequencyInterval);
        validateStartDate(startDate);
        validateEndDate(startDate, endDate);

        LocalDateTime now = LocalDateTime.now();

        PlannedTransaction plannedTransaction =
                new PlannedTransaction();
        plannedTransaction.id = UUID.randomUUID();
        plannedTransaction.userId = userId;
        plannedTransaction.categoryId = categoryId;
        plannedTransaction.amount = amount;
        plannedTransaction.type = type;
        plannedTransaction.description = description;
        plannedTransaction.frequencyType = frequencyType;
        plannedTransaction.frequencyInterval = frequencyInterval;
        plannedTransaction.startDate = startDate;
        plannedTransaction.endDate = endDate;
        plannedTransaction.createdAt = now;
        plannedTransaction.modifiedAt = now;
        plannedTransaction.active = true;
        plannedTransaction.lastExecutedDate = null;
        plannedTransaction.failureCount = 0;

        return plannedTransaction;
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
            LocalDate lastExecutedDate,
            int failureCount
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

        if (createdAt == null) {
            throw new InvalidInputException(
                    "La fecha de creación es obligatoria"
            );
        }
        if (modifiedAt == null) {
            throw new InvalidInputException(
                    "La fecha de modificación es obligatoria"
            );
        }

        PlannedTransaction plannedTransaction =
                new PlannedTransaction();
        plannedTransaction.id = id;
        plannedTransaction.userId = userId;
        plannedTransaction.categoryId = categoryId;
        plannedTransaction.amount = amount;
        plannedTransaction.type = type;
        plannedTransaction.description = description;
        plannedTransaction.frequencyType = frequencyType;
        plannedTransaction.frequencyInterval = frequencyInterval;
        plannedTransaction.startDate = startDate;
        plannedTransaction.endDate = endDate;
        plannedTransaction.createdAt = createdAt;
        plannedTransaction.modifiedAt = modifiedAt;
        plannedTransaction.active = active;
        plannedTransaction.lastExecutedDate = lastExecutedDate;
        plannedTransaction.failureCount = failureCount;

        return plannedTransaction;
    }

    public void updateDetails(
            BigDecimal amount,
            TransactionType type,
            String description,
            FrequencyType frequencyType,
            Integer frequencyInterval,
            UUID categoryId
    ) {
        validateAmount(amount);
        validateType(type);
        validateDescription(description);
        validateFrequencyType(frequencyType);
        validateFrequencyInterval(frequencyInterval);

        this.amount = amount;
        this.type = type;
        this.description = description;
        this.frequencyType = frequencyType;
        this.frequencyInterval = frequencyInterval;
        this.categoryId = categoryId;
        this.modifiedAt = LocalDateTime.now();
    }

    public void markAsExecuted(LocalDate executionDate) {
        if (executionDate == null) {
            throw new InvalidInputException(
                    "La fecha de ejecución es obligatoria"
            );
        }
        if (executionDate.isBefore(startDate)) {
            throw new InvalidInputException(
                    "La fecha de ejecución no puede ser anterior a la fecha de inicio"
            );
        }
        if (endDate != null && executionDate.isAfter(endDate)) {
            throw new InvalidInputException(
                    "La fecha de ejecución no puede ser posterior a la fecha de fin"
            );
        }

        this.lastExecutedDate = executionDate;
        this.modifiedAt = LocalDateTime.now();
    }

    public boolean incrementFailureCount() {
        this.failureCount++;
        this.modifiedAt = LocalDateTime.now();
        return this.failureCount >= MAX_CONSECUTIVE_FAILURES;
    }

    public void resetFailureCount() {
        if (this.failureCount > 0) {
            this.failureCount = 0;
            this.modifiedAt = LocalDateTime.now();
        }
    }

    public void deactivate() {
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new InvalidInputException(
                    "El id es obligatorio"
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidInputException(
                    "El usuario es obligatorio"
            );
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException(
                    "La cantidad debe ser mayor que cero"
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
                    "La descripción no puede estar vacía"
            );
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidInputException(
                    "La descripción no puede superar los "
                            + MAX_DESCRIPTION_LENGTH
                            + " caracteres"
            );
        }
    }

    private static void validateFrequencyType(
            FrequencyType frequencyType
    ) {
        if (frequencyType == null) {
            throw new InvalidInputException(
                    "El tipo de frecuencia es obligatorio"
            );
        }
    }

    private static void validateFrequencyInterval(
            Integer frequencyInterval
    ) {
        if (frequencyInterval == null || frequencyInterval <= 0) {
            throw new InvalidInputException(
                    "El intervalo de frecuencia debe ser mayor que cero"
            );
        }
    }

    private static void validateStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new InvalidInputException(
                    "La fecha de inicio es obligatoria"
            );
        }
    }

    private static void validateEndDate(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidInputException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
    }
}