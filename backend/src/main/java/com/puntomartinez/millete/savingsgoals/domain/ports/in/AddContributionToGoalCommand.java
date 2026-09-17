package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.math.BigDecimal;
import java.util.UUID;

public record AddContributionToGoalCommand(
        UUID goalId,
        UUID userId,
        BigDecimal amount
) {
    public AddContributionToGoalCommand {
        if (goalId == null) {
            throw new InvalidInputException("goalId es obligatorio.");
        }
        if (userId == null) {
            throw new InvalidInputException("userId es obligatorio.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException(
                    "amount debe ser mayor que cero."
            );
        }
    }
}