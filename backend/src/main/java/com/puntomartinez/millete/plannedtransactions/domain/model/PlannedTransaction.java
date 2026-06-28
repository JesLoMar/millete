package com.puntomartinez.millete.plannedtransactions.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
public class PlannedTransaction {

    public enum FrequencyType {
        DAYS, WEEKS, MONTHS, YEARS
    }

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

    public PlannedTransaction(UUID id, UUID userId, UUID categoryId, BigDecimal amount,
                              TransactionType type, String description,
                              FrequencyType frequencyType, Integer frequencyInterval,
                              LocalDate startDate, LocalDate endDate,
                              LocalDateTime createdAt, LocalDateTime modifiedAt, boolean active,
                              LocalDate lastExecutedDate) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("La cantidad no puede ser cero.");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

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
}