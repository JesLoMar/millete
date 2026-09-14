package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddContributionRequestDTO(

        @NotNull(
                message = "El importe de la contribución es obligatorio."
        )
        @DecimalMin(
                value = "0.01",
                message = "El importe de la contribución debe ser mayor que cero."
        )
        BigDecimal amount

) {
}