package com.millete.assistant.domain.ports.in;

import java.util.Objects;

public record InterpretUserInputCommand(
        String input
) {

    public InterpretUserInputCommand {
        Objects.requireNonNull(input, "input cannot be null");

        if (input.isBlank()) {
            throw new IllegalArgumentException("input cannot be blank");
        }
    }
}