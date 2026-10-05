package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.util.Objects;

public record EditRecurringTransactionData(
        RecurringTransactionTarget target,
        RecurringTransactionChanges changes
) implements InterpretationData {

    public EditRecurringTransactionData {
        Objects.requireNonNull(
                target,
                "target cannot be null"
        );

        Objects.requireNonNull(
                changes,
                "changes cannot be null"
        );
    }

    public record RecurringTransactionTarget(
            String description,
            BigDecimal amount,
            String categoryName,
            FrequencyType frequencyType,
            Integer frequencyInterval,
            TransactionType type
    ) {
    }

    public record RecurringTransactionChanges(
            FieldChange<String> description,
            FieldChange<BigDecimal> amount,
            FieldChange<TransactionType> type,
            FieldChange<FrequencyType> frequencyType,
            FieldChange<Integer> frequencyInterval
    ) {

        public RecurringTransactionChanges {
            Objects.requireNonNull(
                    description,
                    "description change cannot be null"
            );

            Objects.requireNonNull(
                    amount,
                    "amount change cannot be null"
            );

            Objects.requireNonNull(
                    type,
                    "type change cannot be null"
            );

            Objects.requireNonNull(
                    frequencyType,
                    "frequencyType change cannot be null"
            );

            Objects.requireNonNull(
                    frequencyInterval,
                    "frequencyInterval change cannot be null"
            );
        }

        public static RecurringTransactionChanges empty() {
            return new RecurringTransactionChanges(
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged(),
                    FieldChange.unchanged()
            );
        }
    }
}