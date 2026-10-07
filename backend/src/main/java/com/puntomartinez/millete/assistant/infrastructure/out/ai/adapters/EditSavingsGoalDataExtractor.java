package com.puntomartinez.millete.assistant.infrastructure.out.ai.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiClient;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiRequest;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditSavingsGoalExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers.EditSavingsGoalExtractionMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public final class EditSavingsGoalDataExtractor
        implements AiDataExtractor {

    private static final String SCHEMA_NAME =
            "edit_savings_goal";

    private static final String SYSTEM_PROMPT = """
        You extract data for the Millete personal finance application.

        The user's input may be written in any language.

        Extract information for editing an existing savings goal.

        The user is NOT creating a new goal.
        The user's first task is to identify which existing goal they want to edit.

        Fields:

        1. targetName
        - The name or natural reference of the existing savings goal.
        - Extract the reference used by the user.
        - Do not invent a goal name.
        - Examples:
          "Cambia mi objetivo vacaciones a coche"
          -> targetName = "vacaciones"

          "Quiero modificar el objetivo para comprar un coche"
          -> targetName = "comprar un coche"

        2. name
        - The NEW name if the user explicitly asks to rename the goal.
        - Return null when the user does not ask to change the name.

        Examples:
          "Cambia vacaciones por Japón"
          -> name = "Japón"

        3. targetAmount
        - The NEW target amount if explicitly requested.
        - Return only the numeric value.
        - Return null when unchanged.

        4. priority
        - The NEW priority if explicitly requested.
        - Allowed values:
          LOW, MEDIUM, HIGH
        - Natural language mappings:
          baja -> LOW
          media -> MEDIUM
          alta -> HIGH
          low -> LOW
          medium -> MEDIUM
          high -> HIGH
          urgente -> HIGH
        - Return null when unchanged.

        5. deadline
        - The NEW deadline when explicitly requested.
        - Return YYYY-MM-DD.
        - The current date is provided separately in this prompt.
        - Interpret relative dates such as:
          "dentro de 6 meses"
          "el año que viene"
        - Return null when unchanged.
        - Never invent a deadline.

        6. link
        - The NEW URL if explicitly requested.
        - Preserve the URL.
        - If the user explicitly asks to remove the link, return an empty string.
        - Return null when the link should remain unchanged.
        - Never invent a URL.

        Important rules:
        - targetName identifies an existing goal.
        - Only extract fields the user explicitly wants to change.
        - Never invent missing values.
        - Do not generate IDs.
        - Do not generate currentAmount.
        - Do not generate active.
        - Do not generate createdAt or modifiedAt.
        - Return null for unchanged fields.
        - Return every schema field.
        - Return only the JSON object matching the required schema.
        """;

    private final StructuredAiClient structuredAiClient;
    private final ObjectMapper objectMapper;

    public EditSavingsGoalDataExtractor(
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

        if (context.action() != AppAction.EDIT_SAVING_GOAL) {
            throw new IllegalArgumentException(
                    "This extractor only supports EDIT_SAVING_GOAL"
            );
        }

        String systemPrompt =
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
                structuredAiClient.generate(request);

        if (rawResponse == null
                || rawResponse.isBlank()) {

            throw new IllegalStateException(
                    "AI returned an empty savings goal edit extraction response"
            );
        }

        try {
            var dto =
                    objectMapper.readValue(
                            rawResponse,
                            EditSavingsGoalExtractionDTO.class
                    );

            return EditSavingsGoalExtractionMapper.toDomain(
                    dto
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not parse AI savings goal edit extraction response",
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

        properties.set(
                "targetName",
                nullableString()
        );

        properties.set(
                "name",
                nullableString()
        );

        properties.set(
                "targetAmount",
                nullableNumber()
        );

        ObjectNode priority =
                nullableString();

        var priorityEnum =
                objectMapper.createArrayNode();

        priorityEnum.add("LOW");
        priorityEnum.add("MEDIUM");
        priorityEnum.add("HIGH");
        priorityEnum.addNull();

        priority.set(
                "enum",
                priorityEnum
        );

        properties.set(
                "priority",
                priority
        );

        properties.set(
                "deadline",
                nullableString()
        );

        properties.set(
                "link",
                nullableString()
        );

        schema.set(
                "properties",
                properties
        );

        var required =
                objectMapper.createArrayNode();

        required.add("targetName");
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

    private ObjectNode nullableString() {
        var node =
                objectMapper.createObjectNode();

        var types =
                objectMapper.createArrayNode();

        types.add("string");
        types.add("null");

        node.set(
                "type",
                types
        );

        return node;
    }

    private ObjectNode nullableNumber() {
        var node =
                objectMapper.createObjectNode();

        var types =
                objectMapper.createArrayNode();

        types.add("number");
        types.add("null");

        node.set(
                "type",
                types
        );

        return node;
    }
}