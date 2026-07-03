package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public record AddContributionToGoalCommand(
        UUID goalId,
        UUID userId,
        BigDecimal amount
) {
    public AddContributionToGoalCommand {
        if (goalId == null) throw new IllegalArgumentException("goalId es obligatorio.");
        if (userId == null) throw new IllegalArgumentException("userId es obligatorio.");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("amount debe ser mayor que cero.");
    }
}
