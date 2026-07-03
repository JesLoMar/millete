package com.puntomartinez.millete.transactions.infrastructure.in.controller.dto;

import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTransactionRequestDTO(
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        BigDecimal amount,

        @NotNull(message = "La fecha es obligatoria")
        LocalDateTime date,

        @NotNull(message = "El tipo de transacción es obligatorio")
        TransactionType type,

        @NotBlank(message = "La descripción no puede estar vacía")
        String description,

        UUID categoryId
) {}
