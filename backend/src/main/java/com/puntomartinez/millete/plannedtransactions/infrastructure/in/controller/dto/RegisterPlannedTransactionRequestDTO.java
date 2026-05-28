package com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterPlannedTransactionRequestDTO(
        UUID categoryId,

        @NotNull(message = "La cantidad es obligatoria")
        BigDecimal amount,

        @NotNull(message = "El tipo es obligatorio")
        TransactionType type,

        @NotBlank(message = "La descripción no puede estar vacía")
        String description,

        @NotNull(message = "El tipo de frecuencia es obligatorio")
        FrequencyType frequencyType,

        @NotNull(message = "El intervalo de frecuencia es obligatorio")
        @Positive(message = "El intervalo debe ser al menos 1")
        Integer frequencyInterval,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,

        LocalDate endDate
) {}