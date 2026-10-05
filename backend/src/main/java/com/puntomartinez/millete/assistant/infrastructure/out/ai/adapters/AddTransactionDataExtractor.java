package com.puntomartinez.millete.assistant.infrastructure.out.ai.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiClient;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiRequest;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddTransactionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers.AddTransactionExtractionMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class AddTransactionDataExtractor
        implements AiDataExtractor {

    private static final String SCHEMA_NAME =
            "add_transaction";

    private static final String SYSTEM_PROMPT = """
            You extract data for the Millete personal finance application.

            The user's input may be written in any language.

            Extract information for creating a new one-time transaction.

            Rules:
            - Extract the transaction description if explicitly provided.
            - Extract the transaction amount if explicitly provided.
            - Extract the category name or category reference if explicitly provided.
            - If a value was not provided, return null.
            - Never invent or guess a description.
            - Never invent or guess an amount.
            - Never invent or guess a category.
            - Do not generate a category ID.
            - Do not generate a transaction ID.
            - Do not generate a date.
            - Do not generate a transaction type.
            - The transaction type is determined by the application action.
            - Return only the JSON object matching the required schema.
            """;

    private final StructuredAiClient structuredAiClient;
    private final ObjectMapper objectMapper;

    public AddTransactionDataExtractor(
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
                != AppAction.ADD_EXPENSE_TRANSACTION) {

            throw new IllegalArgumentException(
                    "This extractor only supports ADD_EXPENSE_TRANSACTION"
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
                    "AI returned an empty transaction extraction response"
            );
        }

        try {
            var dto =
                    objectMapper.readValue(
                            rawResponse,
                            AddTransactionExtractionDTO.class
                    );

            return AddTransactionExtractionMapper.toDomain(
                    dto
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not parse AI transaction extraction response",
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

        var description =
                objectMapper.createObjectNode();

        var descriptionTypes =
                objectMapper.createArrayNode();

        descriptionTypes.add("string");
        descriptionTypes.add("null");

        description.set(
                "type",
                descriptionTypes
        );

        properties.set(
                "description",
                description
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

        var categoryName =
                objectMapper.createObjectNode();

        var categoryTypes =
                objectMapper.createArrayNode();

        categoryTypes.add("string");
        categoryTypes.add("null");

        categoryName.set(
                "type",
                categoryTypes
        );

        properties.set(
                "categoryName",
                categoryName
        );

        schema.set(
                "properties",
                properties
        );

        var required =
                objectMapper.createArrayNode();

        required.add("description");
        required.add("amount");
        required.add("categoryName");

        schema.set(
                "required",
                required
        );

        return schema;
    }
}