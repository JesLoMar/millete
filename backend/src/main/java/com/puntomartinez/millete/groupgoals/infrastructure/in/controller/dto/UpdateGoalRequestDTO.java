package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateGoalRequestDTO(

        @Size(
                max = 100,
                message = "El nombre no puede exceder 100 caracteres."
        )
        String name,

        @DecimalMin(
                value = "0.01",
                message = "El objetivo mensual debe ser mayor que cero."
        )
        BigDecimal monthlyTarget,

        DistributionMode distributionMode

) {
}