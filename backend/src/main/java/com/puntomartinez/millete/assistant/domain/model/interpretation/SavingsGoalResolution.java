package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.util.Objects;

public record SavingsGoalResolution(
        SavingsGoalResolutionStatus status,
        String reference,
        SavingsGoalCandidate goal
) {

    public SavingsGoalResolution {
        Objects.requireNonNull(
                status,
                "status cannot be null"
        );

        Objects.requireNonNull(
                reference,
                "reference cannot be null"
        );

        if (reference.isBlank()) {
            throw new IllegalArgumentException(
                    "reference cannot be blank"
            );
        }

        if (status == SavingsGoalResolutionStatus.FOUND
                && goal == null) {

            throw new IllegalArgumentException(
                    "FOUND resolution requires a savings goal"
            );
        }

        if (status != SavingsGoalResolutionStatus.FOUND
                && goal != null) {

            throw new IllegalArgumentException(
                    "Only FOUND resolution can contain a savings goal"
            );
        }
    }

    public static SavingsGoalResolution found(
            String reference,
            SavingsGoalCandidate goal
    ) {
        return new SavingsGoalResolution(
                SavingsGoalResolutionStatus.FOUND,
                reference,
                Objects.requireNonNull(
                        goal,
                        "goal cannot be null"
                )
        );
    }

    public static SavingsGoalResolution notFound(
            String reference
    ) {
        return new SavingsGoalResolution(
                SavingsGoalResolutionStatus.NOT_FOUND,
                reference,
                null
        );
    }

    public static SavingsGoalResolution ambiguous(
            String reference
    ) {
        return new SavingsGoalResolution(
                SavingsGoalResolutionStatus.AMBIGUOUS,
                reference,
                null
        );
    }
}