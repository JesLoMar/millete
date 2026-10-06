package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.util.Objects;

public record TransactionResolution(
        TransactionResolutionStatus status,
        String reference,
        TransactionCandidate transaction,
        double probability,
        double margin
) {

    public TransactionResolution {
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

        if (probability < 0.0
                || probability > 1.0) {

            throw new IllegalArgumentException(
                    "probability must be between 0 and 1"
            );
        }

        if (margin < 0.0
                || margin > 1.0) {

            throw new IllegalArgumentException(
                    "margin must be between 0 and 1"
            );
        }

        if (status == TransactionResolutionStatus.FOUND
                && transaction == null) {

            throw new IllegalArgumentException(
                    "FOUND resolution requires a transaction"
            );
        }

        if (status != TransactionResolutionStatus.FOUND
                && transaction != null) {

            throw new IllegalArgumentException(
                    "Only FOUND resolution can contain a transaction"
            );
        }
    }

    public static TransactionResolution found(
            String reference,
            TransactionCandidate transaction,
            double probability,
            double margin
    ) {
        return new TransactionResolution(
                TransactionResolutionStatus.FOUND,
                reference,
                Objects.requireNonNull(
                        transaction,
                        "transaction cannot be null"
                ),
                probability,
                margin
        );
    }

    public static TransactionResolution notFound(
            String reference,
            double probability,
            double margin
    ) {
        return new TransactionResolution(
                TransactionResolutionStatus.NOT_FOUND,
                reference,
                null,
                probability,
                margin
        );
    }

    public static TransactionResolution ambiguous(
            String reference,
            double probability,
            double margin
    ) {
        return new TransactionResolution(
                TransactionResolutionStatus.AMBIGUOUS,
                reference,
                null,
                probability,
                margin
        );
    }
}