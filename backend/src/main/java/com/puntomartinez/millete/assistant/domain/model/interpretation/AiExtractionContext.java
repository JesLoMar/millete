package com.puntomartinez.millete.assistant.domain.model.interpretation;

import com.puntomartinez.millete.assistant.domain.model.AppAction;

import java.util.Objects;

public record AiExtractionContext(
        String input,
        AppAction action
) {

    public AiExtractionContext {
        Objects.requireNonNull(
                input,
                "input cannot be null"
        );

        if (input.isBlank()) {
            throw new IllegalArgumentException(
                    "input cannot be blank"
            );
        }

        Objects.requireNonNull(
                action,
                "action cannot be null"
        );
    }
}