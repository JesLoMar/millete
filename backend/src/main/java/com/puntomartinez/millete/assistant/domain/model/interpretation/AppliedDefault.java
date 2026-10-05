package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.util.Objects;

public record AppliedDefault(
        String field,
        String reason
) {

    public AppliedDefault {
        Objects.requireNonNull(
                field,
                "field cannot be null"
        );

        Objects.requireNonNull(
                reason,
                "reason cannot be null"
        );

        if (field.isBlank()) {
            throw new IllegalArgumentException(
                    "field cannot be blank"
            );
        }

        if (reason.isBlank()) {
            throw new IllegalArgumentException(
                    "reason cannot be blank"
            );
        }
    }
}