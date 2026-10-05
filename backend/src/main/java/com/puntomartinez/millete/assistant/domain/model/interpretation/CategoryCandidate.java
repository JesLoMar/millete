package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.util.Objects;
import java.util.UUID;

public record CategoryCandidate(
        UUID id,
        String name
) {

    public CategoryCandidate {
        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }
    }
}