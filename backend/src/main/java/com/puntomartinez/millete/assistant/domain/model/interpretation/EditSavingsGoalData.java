package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record EditSavingsGoalData(
        SavingsGoalTarget target,
        SavingsGoalChanges changes
) implements InterpretationData {

    public EditSavingsGoalData {
        Objects.requireNonNull(
                target,
                "target cannot be null"
        );

        Objects.requireNonNull(
                changes,
                "changes cannot be null"
        );
    }

    public record SavingsGoalTarget(
            UUID id,
            String name
    ) {

        public boolean hasCriteria() {
            return name != null
                    && !name.isBlank();
        }
    }

    public record SavingsGoalChanges(
            FieldChange<String> name,
            FieldChange<BigDecimal> targetAmount,
            FieldChange<SavingsGoalPriority> priority,
            FieldChange<LocalDate> deadline,
            FieldChange<String> link
    ) {

        public SavingsGoalChanges {
            Objects.requireNonNull(
                    name,
                    "name change cannot be null"
            );

            Objects.requireNonNull(
                    targetAmount,
                    "targetAmount change cannot be null"
            );

            Objects.requireNonNull(
                    priority,
                    "priority change cannot be null"
            );

            Objects.requireNonNull(
                    deadline,
                    "deadline change cannot be null"
            );

            Objects.requireNonNull(
                    link,
                    "link change cannot be null"
            );
        }

        public boolean hasChanges() {
            return name.specified()
                    || targetAmount.specified()
                    || priority.specified()
                    || deadline.specified()
                    || link.specified();
        }

        public static SavingsGoalChanges empty() {
            return new SavingsGoalChanges(
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged()
            );
        }
    }
}