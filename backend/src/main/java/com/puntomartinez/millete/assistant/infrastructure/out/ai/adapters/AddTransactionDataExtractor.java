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

        The supported actions are:
        - ADD_EXPENSE_TRANSACTION: create a one-time expense.
        - ADD_INCOME_TRANSACTION: create a one-time income.

        Fields:

        1. description
        - A short description of what the transaction is about.
        - Do NOT copy the whole user sentence.
        - Prefer the concise concept or reason of the transaction.
        - Example:
          "He cobrado 1500 euros de nómina"
          -> description = "nómina"

        2. amount
        - The numeric transaction amount explicitly mentioned by the user.
        - Example:
          "He cobrado 1500 euros de nómina"
          -> amount = 1500

        3. categoryName
        - The category name or category reference explicitly mentioned or clearly identified in the user input.
        - A category reference can be expressed naturally inside the sentence.
        - Example:
          "He cobrado 1500 euros de nómina"
          -> categoryName = "Nómina"
        - Example:
          "He pagado 40 euros de gasolina"
          -> categoryName = "Gasolina"
        - Example:
          "Me han ingresado 2000 euros por mi nómina"
          -> categoryName = "Nómina"

        Important distinction:
        - The full user sentence is NOT the description.
        - When a phrase contains both a transaction concept and a category reference,
          extract the concise transaction concept as description and the category reference as categoryName.

        Rules:
        - If a value was not explicitly provided, return null.
        - Never invent or guess a description.
        - Never invent or guess an amount.
        - Never invent or guess a category.
        - Do not generate a category ID.
        - Do not generate a transaction ID.
        - Do not generate a date.
        - Do not generate a transaction type.
        - The transaction type is determined by the application action.
        - ADD_EXPENSE_TRANSACTION means EXPENSE.
        - ADD_INCOME_TRANSACTION means INCOME.
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

        validateAction(
                context.action()
        );

        var request =
                new StructuredAiRequest(
                        SYSTEM_PROMPT,
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

    private void validateAction(
            AppAction action
    ) {
        if (action != AppAction.ADD_EXPENSE_TRANSACTION
                && action != AppAction.ADD_INCOME_TRANSACTION) {

            throw new IllegalArgumentException(
                    "This extractor only supports ADD_EXPENSE_TRANSACTION and ADD_INCOME_TRANSACTION"
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