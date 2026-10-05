package com.puntomartinez.millete.assistant.domain.ports.in;

import java.util.Objects;
import java.util.UUID;

public record InterpretUserInputCommand(
        UUID userId,
        String input
) {

    public InterpretUserInputCommand {
        Objects.requireNonNull(
                userId,
                "userId cannot be null"
        );

        Objects.requireNonNull(
                input,
                "input cannot be null"
        );

        if (input.isBlank()) {
            throw new IllegalArgumentException(
                    "input cannot be blank"
            );
        }
    }
}