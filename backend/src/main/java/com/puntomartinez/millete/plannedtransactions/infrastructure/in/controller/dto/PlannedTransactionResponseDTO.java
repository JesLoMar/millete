package com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PlannedTransactionResponseDTO(
        UUID id,
        UUID categoryId,
        BigDecimal amount,
        TransactionType type,
        String description,
        FrequencyType frequencyType,
        Integer frequencyInterval,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {}