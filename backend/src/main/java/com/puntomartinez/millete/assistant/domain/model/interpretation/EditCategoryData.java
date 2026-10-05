package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.util.Objects;

public record EditCategoryData(
        CategoryTarget target,
        CategoryChanges changes
) implements InterpretationData {

    public EditCategoryData {
        Objects.requireNonNull(
                target,
                "target cannot be null"
        );

        Objects.requireNonNull(
                changes,
                "changes cannot be null"
        );
    }

    public record CategoryTarget(
            String name
    ) {

        public CategoryTarget {
            if (name != null && name.isBlank()) {
                throw new IllegalArgumentException(
                        "Target name cannot be blank"
                );
            }
        }
    }

    public record CategoryChanges(
            FieldChange<String> name,
            FieldChange<String> description,
            FieldChange<BigDecimal> budgetLimit,
            FieldChange<String> color
    ) {

        public CategoryChanges {
            Objects.requireNonNull(
                    name,
                    "name change cannot be null"
            );

            Objects.requireNonNull(
                    description,
                    "description change cannot be null"
            );

            Objects.requireNonNull(
                    budgetLimit,
                    "budgetLimit change cannot be null"
            );

            Objects.requireNonNull(
                    color,
                    "color change cannot be null"
            );
        }

        public static CategoryChanges empty() {
            return new CategoryChanges(
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged()
            );
        }
    }
}