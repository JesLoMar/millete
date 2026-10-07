package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record AddSavingsGoalContributionData(
        SavingsGoalTarget target,
        BigDecimal amount
) implements InterpretationData {

    public AddSavingsGoalContributionData {
        Objects.requireNonNull(
                target,
                "target cannot be null"
        );
    }

    public record SavingsGoalTarget(
            UUID id,
            String name
    ) {
    }
}