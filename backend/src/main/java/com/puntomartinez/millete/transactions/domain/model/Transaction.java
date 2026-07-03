package com.puntomartinez.millete.transactions.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
public class Transaction {


    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private BigDecimal amount;
    private LocalDateTime date;
    private TransactionType type;
    private String description;


    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;


    public enum TransactionType {
        INCOME,
        EXPENSE
    }


    public Transaction(UUID id, UUID userId, UUID categoryId, BigDecimal amount,
                       LocalDateTime date, TransactionType type, String description,
                       LocalDateTime createdAt, LocalDateTime modifiedAt, boolean active) {


        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

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

    public void updateDetails(BigDecimal amount, LocalDateTime date, TransactionType type, String description, UUID categoryId) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        this.amount = amount;
        this.date = date;
        this.type = type;
        this.description = description;
        this.categoryId = categoryId;

        this.modifiedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

}
