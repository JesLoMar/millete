package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.util.Objects;

public record MissingField(
        String field
) {

    public MissingField {
        Objects.requireNonNull(
                field,
                "field cannot be null"
        );

        if (field.isBlank()) {
            throw new IllegalArgumentException(
                    "field cannot be blank"
            );
        }
    }
}