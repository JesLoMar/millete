package com.puntomartinez.millete.assistant.infrastructure.out.ai.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiClient;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiRequest;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddSavingsGoalExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers.AddSavingsGoalExtractionMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public final class AddSavingsGoalDataExtractor
        implements com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor {

    private static final String SCHEMA_NAME =
            "add_savings_goal";

    private static final String SYSTEM_PROMPT = """
        You extract data for the Millete personal finance application.

        The user's input may be written in any language.

        Extract information for creating a new savings goal.

        Fields:

        1. name
        - The concise name of the savings goal.
        - Extract the main concept or purpose of the goal.
        - Do NOT copy the whole user sentence.
        - Prefer a short natural name suitable for a form field.
        - Example:
          "Quiero ahorrar 5000 euros para comprar un coche"
          -> name = "coche"
        - Example:
          "Crea un objetivo para las vacaciones"
          -> name = "vacaciones"
        - Example:
          "Quiero guardar dinero para una reforma"
          -> name = "reforma"

        2. targetAmount
        - The target monetary amount explicitly mentioned by the user.
        - Return only the numeric value.
        - Examples:
          "Quiero ahorrar 3000 euros para el coche"
          -> targetAmount = 3000
        - If the user does not provide a target amount, return null.
        - Never invent an amount.

        3. priority
        - Extract the priority only when the user explicitly provides it.
        - Map natural language to:
          - LOW: baja, low
          - MEDIUM: media, medio, normal, medium
          - HIGH: alta, alto, urgente, high
        - Return only LOW, MEDIUM or HIGH.
        - If the user does not specify a priority, return null.
        - Do NOT assume MEDIUM here; the application applies that default.

        4. deadline
        - Extract the deadline only when the user provides enough information to determine a concrete date.
        - Return the date strictly as YYYY-MM-DD.
        - The current date is provided separately in this prompt.
        - Interpret relative dates such as "dentro de 6 meses", "en un año" or
          "el mes que viene" using the provided current date.
        - Do not invent a deadline when the expression is ambiguous.
        - If no deadline is provided, return null.

        5. link
        - Extract an explicit URL if the user provides one.
        - Preserve the URL as provided.
        - Do NOT invent a URL.
        - If no URL is provided, return null.

        General rules:
        - Return only information supported by the user's input.
        - Never invent missing values.
        - Do not generate IDs.
        - Do not generate currentAmount.
        - Do not generate status.
        - Do not generate createdAt or modifiedAt.
        - Do not include fields outside the schema.
        - Return only the JSON object matching the required schema.
        """;

    private final StructuredAiClient structuredAiClient;
    private final ObjectMapper objectMapper;

    public AddSavingsGoalDataExtractor(
            StructuredAiClient structuredAiClient,
            ObjectMapper objectMapper
    ) {
        this.structuredAiClient =
                Objects.requireNonNull(
                        structuredAiClient,
                        "structuredAiClient cannot be null"
                );

        this.objectMapper =
                Objects.requireNonNull(
                        objectMapper,
                        "objectMapper cannot be null"
                );
    }

    @Override
    public InterpretationData extract(
            AiExtractionContext context
    ) {
        Objects.requireNonNull(
                context,
                "context cannot be null"
        );

        if (context.action() != AppAction.ADD_SAVING_GOAL) {
            throw new IllegalArgumentException(
                    "This extractor only supports ADD_SAVING_GOAL"
            );
        }

        var systemPrompt =
                SYSTEM_PROMPT
                        + "\nCurrent date: "
                        + LocalDate.now()
                        + "\n";

        var request =
                new StructuredAiRequest(
                        systemPrompt,
                        context.input(),
                        SCHEMA_NAME,
                        buildResponseSchema()
                );

        var rawResponse =
                structuredAiClient.generate(
                        request
                );

        if (rawResponse == null
                || rawResponse.isBlank()) {

            throw new IllegalStateException(
                    "AI returned an empty savings goal extraction response"
            );
        }

        try {
            var dto =
                    objectMapper.readValue(
                            rawResponse,
                            AddSavingsGoalExtractionDTO.class
                    );

            return AddSavingsGoalExtractionMapper.toDomain(
                    dto
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Could not parse AI savings goal extraction response",
                    exception
            );
        }
    }

    private JsonNode buildResponseSchema() {

        var schema =
                objectMapper.createObjectNode();

        schema.put(
                "type",
                "object"
        );

        schema.put(
                "additionalProperties",
                false
        );

        var properties =
                objectMapper.createObjectNode();

        var name =
                nullableType("string");

        properties.set(
                "name",
                name
        );

        var targetAmount =
                nullableType("number");

        properties.set(
                "targetAmount",
                targetAmount
        );

        var priority =
                nullableType("string");

        properties.set(
                "priority",
                priority
        );

        var deadline =
                nullableType("string");

        properties.set(
                "deadline",
                deadline
        );

        var link =
                nullableType("string");

        properties.set(
                "link",
                link
        );

        schema.set(
                "properties",
                properties
        );

        var required =
                objectMapper.createArrayNode();

        required.add("name");
        required.add("targetAmount");
        required.add("priority");
        required.add("deadline");
        required.add("link");

        schema.set(
                "required",
                required
        );

        return schema;
    }

    private JsonNode nullableType(
            String type
    ) {
        var node =
                objectMapper.createObjectNode();

        var types =
                objectMapper.createArrayNode();

        types.add(type);
        types.add("null");

        node.set(
                "type",
                types
        );

        return node;
    }
}