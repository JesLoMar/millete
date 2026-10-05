package com.puntomartinez.millete.assistant.infrastructure.out.ai.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddTransactionData;
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

    private static final String SYSTEM_PROMPT = """
            Extract transaction data from the user's input.

            Supported actions:
            - ADD_EXPENSE_TRANSACTION: register a one-off expense.
            - ADD_INCOME_TRANSACTION: register a one-off income.

            The user input can be written in any language.

            Extract only information that is explicitly present in the user input.

            Fields:
            - description: transaction description.
            - amount: transaction amount.
            - categoryName: the category name explicitly referenced by the user.

            Rules:
            - Return null for any field that was not explicitly provided.
            - Never invent missing information.
            - Never generate category IDs.
            - Never generate transaction dates.
            - Never generate transaction type.
            - Transaction type is determined exclusively by the action.
            - ADD_EXPENSE_TRANSACTION means EXPENSE.
            - ADD_INCOME_TRANSACTION means INCOME.
            - Do not add any field other than the fields defined by the schema.
            """;

    private static final String SCHEMA_NAME =
            "millete_add_transaction";

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

        var responseSchema =
                buildSchema();

        var request =
                new StructuredAiRequest(
                        SYSTEM_PROMPT,
                        context.input(),
                        SCHEMA_NAME,
                        responseSchema
                );

        var rawResponse =
                structuredAiClient.generate(
                        request
                );

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
                    "Could not parse transaction AI extraction response",
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
                    "Unsupported action for AddTransactionDataExtractor: "
                            + action
            );
        }
    }

    private JsonNode buildSchema() {
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
                schema.putObject(
                        "properties"
                );

        properties
                .putObject("description")
                .putArray("type")
                .add("string")
                .add("null");

        properties
                .putObject("amount")
                .putArray("type")
                .add("number")
                .add("null");

        properties
                .putObject("categoryName")
                .putArray("type")
                .add("string")
                .add("null");

        schema
                .putArray("required")
                .add("description")
                .add("amount")
                .add("categoryName");

        return schema;
    }
}