package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record RecurringTransactionResolution(
        RecurringTransactionResolutionStatus status,
        String reference,
        RecurringTransactionCandidate transaction
) {

    public RecurringTransactionResolution {
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

        if (status == RecurringTransactionResolutionStatus.FOUND
                && transaction == null) {

            throw new IllegalArgumentException(
                    "FOUND resolution requires a recurring transaction"
            );
        }

        if (status != RecurringTransactionResolutionStatus.FOUND
                && transaction != null) {

            throw new IllegalArgumentException(
                    "Only FOUND resolution can contain a recurring transaction"
            );
        }
    }

    public static RecurringTransactionResolution found(
            String reference,
            RecurringTransactionCandidate transaction
    ) {
        return new RecurringTransactionResolution(
                RecurringTransactionResolutionStatus.FOUND,
                reference,
                Objects.requireNonNull(
                        transaction,
                        "transaction cannot be null"
                )
        );
    }

    public static RecurringTransactionResolution notFound(
            String reference
    ) {
        return new RecurringTransactionResolution(
                RecurringTransactionResolutionStatus.NOT_FOUND,
                reference,
                null
        );
    }

    public static RecurringTransactionResolution ambiguous(
            String reference
    ) {
        return new RecurringTransactionResolution(
                RecurringTransactionResolutionStatus.AMBIGUOUS,
                reference,
                null
        );
    }

    public record RecurringTransactionCandidate(
            UUID id,
            String description,
            BigDecimal amount,
            String categoryName,
            FrequencyType frequencyType,
            Integer frequencyInterval,
            TransactionType type
    ) {

        public RecurringTransactionCandidate {
            Objects.requireNonNull(
                    id,
                    "id cannot be null"
            );

            Objects.requireNonNull(
                    description,
                    "description cannot be null"
            );

            Objects.requireNonNull(
                    amount,
                    "amount cannot be null"
            );

            Objects.requireNonNull(
                    frequencyType,
                    "frequencyType cannot be null"
            );

            Objects.requireNonNull(
                    frequencyInterval,
                    "frequencyInterval cannot be null"
            );

            Objects.requireNonNull(
                    type,
                    "type cannot be null"
            );
        }
    }

    public enum RecurringTransactionResolutionStatus {
        FOUND,
        NOT_FOUND,
        AMBIGUOUS
    }
}