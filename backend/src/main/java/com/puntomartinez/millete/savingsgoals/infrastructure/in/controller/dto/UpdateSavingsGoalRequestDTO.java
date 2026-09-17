package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto;

import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateSavingsGoalRequestDTO(
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres.")
        String name,

        @DecimalMin(value = "0.01", message = "El monto objetivo debe ser mayor que cero.")
        BigDecimal targetAmount,

        LocalDate deadline,

        GoalPriority priority,

        @Size(max = 500, message = "El link no puede exceder 500 caracteres.")
        String link
) {}