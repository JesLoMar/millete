package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record EditTransactionData(
        TransactionTarget target,
        TransactionChanges changes
) implements InterpretationData {

    public EditTransactionData {
        Objects.requireNonNull(
                target,
                "target cannot be null"
        );

        Objects.requireNonNull(
                changes,
                "changes cannot be null"
        );
    }

    public record TransactionTarget(
            UUID id,
            String description,
            BigDecimal amount,
            String categoryName,
            LocalDate date,
            TransactionType type
    ) {

        public TransactionTarget {
            if (description != null
                    && description.isBlank()) {

                throw new IllegalArgumentException(
                        "Target description cannot be blank"
                );
            }

            if (categoryName != null
                    && categoryName.isBlank()) {

                throw new IllegalArgumentException(
                        "Target categoryName cannot be blank"
                );
            }
        }

        public boolean hasCriteria() {
            return description != null
                    || amount != null
                    || categoryName != null
                    || date != null
                    || type != null;
        }
    }

    public record TransactionChanges(
            FieldChange<String> description,
            FieldChange<BigDecimal> amount,
            FieldChange<String> categoryName,
            FieldChange<TransactionType> type
    ) {

        public TransactionChanges {
            Objects.requireNonNull(
                    description,
                    "description change cannot be null"
            );

            Objects.requireNonNull(
                    amount,
                    "amount change cannot be null"
            );

            Objects.requireNonNull(
                    categoryName,
                    "categoryName change cannot be null"
            );

            Objects.requireNonNull(
                    type,
                    "type change cannot be null"
            );
        }

        public boolean hasChanges() {
            return description.specified()
                    || amount.specified()
                    || categoryName.specified()
                    || type.specified();
        }

        public static TransactionChanges empty() {
            return new TransactionChanges(
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged()
            );
        }
    }
}