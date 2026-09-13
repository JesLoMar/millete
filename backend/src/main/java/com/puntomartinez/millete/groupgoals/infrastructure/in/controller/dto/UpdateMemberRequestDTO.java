package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateMemberRequestDTO(

        GoalRole role,

        @DecimalMin(
                value = "0.00",
                message = "El salario no puede ser negativo."
        )
        BigDecimal salary,

        @DecimalMin(
                value = "0.00",
                message = "El porcentaje no puede ser negativo."
        )
        @DecimalMax(
                value = "100.00",
                message = "El porcentaje no puede superar 100."
        )
        BigDecimal customPercentage

) {
}