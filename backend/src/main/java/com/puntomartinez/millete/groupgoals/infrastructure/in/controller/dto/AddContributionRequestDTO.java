package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record AddContributionRequestDTO(

        @DecimalMin(
                value = "0.01",
                message = "El importe de la contribución debe ser mayor que cero."
        )
        BigDecimal amount

) {
}