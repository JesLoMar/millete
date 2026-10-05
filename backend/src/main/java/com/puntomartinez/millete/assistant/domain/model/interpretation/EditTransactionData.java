package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

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
            String description,
            BigDecimal amount,
            String categoryName,
            LocalDate date,
            TransactionType type
    ) {
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