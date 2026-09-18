package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawContributionRequestDTO(
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(
                value = "0.01",
                message = "La cantidad debe ser mayor que cero"
        )
        BigDecimal amount
) {
}