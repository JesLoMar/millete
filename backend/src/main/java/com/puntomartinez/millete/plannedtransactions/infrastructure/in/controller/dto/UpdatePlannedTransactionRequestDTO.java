package com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdatePlannedTransactionRequestDTO(
        UUID categoryId,

        @NotNull BigDecimal amount,

        @NotNull TransactionType type,

        @NotBlank String description,

        @NotNull FrequencyType frequencyType,

        @NotNull Integer frequencyInterval,

        @NotNull LocalDate startDate,

        LocalDate endDate
) {}