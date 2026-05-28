package com.puntomartinez.millete.transactions.infrastructure.in.controller.dto;

import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterTransactionRequestDTO(
        UUID categoryId, // Puede ser nulo si no le asigna etiqueta

        @NotNull(message = "La cantidad es obligatoria")
        BigDecimal amount,

        @NotNull(message = "La fecha es obligatoria")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime date,

        @NotNull(message = "El tipo de transacción es obligatorio")
        TransactionType type,

        @NotBlank(message = "La descripción no puede estar vacía")
        String description
) {}