package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record SavingsGoalCandidate(
        UUID id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        SavingsGoalPriority priority,
        LocalDate deadline,
        String link
) {

    public SavingsGoalCandidate {
        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }

        Objects.requireNonNull(
                targetAmount,
                "targetAmount cannot be null"
        );

        Objects.requireNonNull(
                currentAmount,
                "currentAmount cannot be null"
        );

        Objects.requireNonNull(
                priority,
                "priority cannot be null"
        );
    }
}
