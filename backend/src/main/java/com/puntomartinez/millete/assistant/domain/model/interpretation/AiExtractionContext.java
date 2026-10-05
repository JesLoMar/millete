package com.puntomartinez.millete.assistant.domain.model.interpretation;

import com.puntomartinez.millete.assistant.domain.model.AppAction;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Objects;

public record AiExtractionContext(
        String input,
        AppAction action,
        ZonedDateTime now,
        Locale locale,
        String currencyCode
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

        Objects.requireNonNull(
                now,
                "now cannot be null"
        );

        Objects.requireNonNull(
                locale,
                "locale cannot be null"
        );

        Objects.requireNonNull(
                currencyCode,
                "currencyCode cannot be null"
        );

        if (currencyCode.isBlank()) {
            throw new IllegalArgumentException(
                    "currencyCode cannot be blank"
            );
        }
    }
}