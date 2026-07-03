package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSavingsGoalCommand(
        UUID userId,
        String name,
        BigDecimal targetAmount,
        LocalDate deadline,
        String priority,
        String link
) {
    public CreateSavingsGoalCommand {
        if (userId == null) throw new IllegalArgumentException("userId es obligatorio.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name es obligatorio.");
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("targetAmount debe ser mayor que cero.");
        if (deadline != null && !deadline.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("deadline debe ser posterior a hoy.");
    }
}
