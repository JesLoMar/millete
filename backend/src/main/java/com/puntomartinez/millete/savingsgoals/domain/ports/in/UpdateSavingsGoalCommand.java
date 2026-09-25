package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateSavingsGoalCommand(
        UUID id,
        UUID userId,
        String name,
        BigDecimal targetAmount,
        LocalDate deadline,
        GoalPriority priority,
        String link
) {
    public UpdateSavingsGoalCommand {
        if (id == null) {
            throw new IllegalArgumentException("id es obligatorio.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId es obligatorio.");
        }
        if (name != null && name.length() > 100) {
            throw new IllegalArgumentException(
                    "name no puede superar los 100 caracteres."
            );
        }
        if (targetAmount != null
                && targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "targetAmount debe ser mayor que cero."
            );
        }
    }
}
