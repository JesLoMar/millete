package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record TransactionCandidate(
        UUID id,
        String description,
        BigDecimal amount,
        String categoryName,
        LocalDate date,
        TransactionType type
) {

    public TransactionCandidate {
        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        Objects.requireNonNull(
                description,
                "description cannot be null"
        );

        if (description.isBlank()) {
            throw new IllegalArgumentException(
                    "description cannot be blank"
            );
        }

        Objects.requireNonNull(
                amount,
                "amount cannot be null"
        );

        Objects.requireNonNull(
                date,
                "date cannot be null"
        );

        Objects.requireNonNull(
                type,
                "type cannot be null"
        );
    }
}