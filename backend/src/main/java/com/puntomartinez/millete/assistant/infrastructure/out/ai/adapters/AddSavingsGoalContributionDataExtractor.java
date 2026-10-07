package com.puntomartinez.millete.assistant.infrastructure.out.ai.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiClient;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiRequest;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddSavingsGoalContributionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers.AddSavingsGoalContributionExtractionMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class AddSavingsGoalContributionDataExtractor
        implements AiDataExtractor {

    private static final String SCHEMA_NAME =
            "add_savings_goal_contribution";

    private static final String SYSTEM_PROMPT = """
        You extract data for the Millete personal finance application.

        The user's input may be written in any language.

        Extract information for adding money to an existing savings goal.

        The user is NOT creating a new savings goal.
        The user wants to add a monetary contribution to an existing goal.

        Fields:

        1. targetName
        - The name or natural reference of the existing savings goal.
        - Extract the goal reference used by the user.
        - Never invent a goal name.
        - Examples:
          "Añade 300 euros al objetivo coche"
          -> targetName = "coche"

          "He ahorrado 150 € para las vacaciones"
          -> targetName = "vacaciones"

        2. amount
        - The contribution amount explicitly mentioned by the user.
        - Return only the numeric value.
        - Never invent the amount.

        Important rules:
        - This action adds money to an existing goal.
        - Do not extract or generate a goal ID.
        - Do not generate currentAmount.
        - Do not generate a new target amount.
        - If a value is not provided, return null.
        - Return every field required by the schema.
        - Return only the JSON object matching the required schema.
        """;

    private final StructuredAiClient structuredAiClient;
    private final ObjectMapper objectMapper;

    public AddSavingsGoalContributionDataExtractor(
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

        if (context.action()
                != AppAction.ADD_SAVING_GOAL_CONTRIBUTION) {

            throw new IllegalArgumentException(
                    "This extractor only supports "
                            + "ADD_SAVING_GOAL_CONTRIBUTION"
            );
        }

        var request =
                new StructuredAiRequest(
                        SYSTEM_PROMPT,
                        context.input(),
                        SCHEMA_NAME,
                        buildResponseSchema()
                );

        var rawResponse =
                structuredAiClient.generate(request);

        if (rawResponse == null
                || rawResponse.isBlank()) {

            throw new IllegalStateException(
                    "AI returned an empty savings goal contribution extraction response"
            );
        }

        try {
            var dto =
                    objectMapper.readValue(
                            rawResponse,
                            AddSavingsGoalContributionExtractionDTO.class
                    );

            return AddSavingsGoalContributionExtractionMapper.toDomain(
                    dto
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not parse AI savings goal contribution extraction response",
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

        var targetName =
                objectMapper.createObjectNode();

        var targetTypes =
                objectMapper.createArrayNode();

        targetTypes.add("string");
        targetTypes.add("null");

        targetName.set(
                "type",
                targetTypes
        );

        properties.set(
                "targetName",
                targetName
        );

        var amount =
                objectMapper.createObjectNode();

        var amountTypes =
                objectMapper.createArrayNode();

        amountTypes.add("number");
        amountTypes.add("null");

        amount.set(
                "type",
                amountTypes
        );

        properties.set(
                "amount",
                amount
        );

        schema.set(
                "properties",
                properties
        );

        var required =
                objectMapper.createArrayNode();

        required.add("targetName");
        required.add("amount");

        schema.set(
                "required",
                required
        );

        return schema;
    }
}