package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddContributionRequestDTO(
        @NotNull(message = "La cantidad es obligatoria.")
        @DecimalMin(value = "0.01", message = "La contribución debe ser mayor que cero.")
        BigDecimal amount
) {}