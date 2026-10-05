package com.puntomartinez.millete.assistant.infrastructure.out.ai.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public record StructuredAiRequest(
        String systemPrompt,
        String userPrompt,
        String schemaName,
        JsonNode responseSchema
) {

    public StructuredAiRequest {
        Objects.requireNonNull(
                systemPrompt,
                "systemPrompt cannot be null"
        );

        Objects.requireNonNull(
                userPrompt,
                "userPrompt cannot be null"
        );

        Objects.requireNonNull(
                schemaName,
                "schemaName cannot be null"
        );

        Objects.requireNonNull(
                responseSchema,
                "responseSchema cannot be null"
        );

        if (systemPrompt.isBlank()) {
            throw new IllegalArgumentException(
                    "systemPrompt cannot be blank"
            );
        }

        if (userPrompt.isBlank()) {
            throw new IllegalArgumentException(
                    "userPrompt cannot be blank"
            );
        }

        if (schemaName.isBlank()) {
            throw new IllegalArgumentException(
                    "schemaName cannot be blank"
            );
        }

        if (!responseSchema.isObject()) {
            throw new IllegalArgumentException(
                    "responseSchema must be a JSON object"
            );
        }
    }
}