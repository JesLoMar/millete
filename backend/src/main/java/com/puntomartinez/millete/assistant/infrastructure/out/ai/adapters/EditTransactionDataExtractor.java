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
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditTransactionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers.EditTransactionExtractionMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class EditTransactionDataExtractor
        implements AiDataExtractor {

    private static final String SCHEMA_NAME =
            "edit_transaction";

    private static final String SYSTEM_PROMPT = """
            You extract data for editing an existing transaction in the
            Millete personal finance application.

            The user's input may be written in any language.

            The output has two separate parts:

            1. target
            Describes the existing transaction the user wants to edit.
            Extract identifying information that refers to the original
            transaction.

            Possible target fields:
            - description
            - amount
            - categoryName
            - date
            - type

            2. changes
            Describes only the fields the user explicitly wants to change.

            Each change contains:
            - specified: true when the user explicitly wants to change
              that field.
            - specified: false when the user does not mention changing
              that field.
            - value: the new value when specified is true.
            - value may be null only when the user explicitly wants to
              clear an optional field.

            Important:
            - Do NOT confuse target values with new values.
            - Example:
              "Cambia el gasto de gasolina de 35 a 40 euros"
              target:
                description = "gasolina"
                amount = 35
              changes:
                amount = 40
            - Example:
              "Cambia gasolina de 35 euros a transporte"
              target:
                description = "gasolina"
                amount = 35
              changes:
                categoryName = "transporte"
            - Example:
              "Modifica la nómina de 1500 a 1800 euros"
              target:
                description = "nómina"
                amount = 1500
              changes:
                amount = 1800

            Rules:
            - Never invent target information.
            - Never invent changes.
            - Do not generate transaction IDs.
            - The application resolves the target transaction ID.
            - Do not generate category IDs.
            - The application resolves categories.
            - Do not generate any field that is not in the schema.
            - Date is used only to identify the existing transaction.
            - Date is not an editable change.
            - Type can only be INCOME or EXPENSE.
            - If the user does not explicitly change a field, set
              specified=false and value=null.
            - If the user explicitly wants to clear the category, set
              specified=true and value=null for categoryName.
            - Return only the JSON object matching the required schema.
            """;

    private final StructuredAiClient structuredAiClient;
    private final ObjectMapper objectMapper;

    public EditTransactionDataExtractor(
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
                != AppAction.EDIT_TRANSACTION) {

            throw new IllegalArgumentException(
                    "This extractor only supports EDIT_TRANSACTION"
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
                structuredAiClient.generate(
                        request
                );

        if (rawResponse == null
                || rawResponse.isBlank()) {

            throw new IllegalStateException(
                    "AI returned an empty edit transaction extraction response"
            );
        }

        try {
            var dto =
                    objectMapper.readValue(
                            rawResponse,
                            EditTransactionExtractionDTO.class
                    );

            return EditTransactionExtractionMapper.toDomain(
                    dto
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not parse AI edit transaction extraction response",
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

        ObjectNode properties =
                schema.putObject(
                        "properties"
                );

        properties.set(
                "target",
                buildTargetSchema()
        );

        properties.set(
                "changes",
                buildChangesSchema()
        );

        schema.putArray(
                        "required"
                )
                .add("target")
                .add("changes");

        return schema;
    }

    private JsonNode buildTargetSchema() {
        var target =
                objectMapper.createObjectNode();

        target.put(
                "type",
                "object"
        );

        target.put(
                "additionalProperties",
                false
        );

        ObjectNode properties =
                target.putObject(
                        "properties"
                );

        addNullableStringProperty(
                properties,
                "description"
        );

        addNullableNumberProperty(
                properties,
                "amount"
        );

        addNullableStringProperty(
                properties,
                "categoryName"
        );

        addNullableStringProperty(
                properties,
                "date"
        );

        addNullableStringProperty(
                properties,
                "type"
        );

        target.putArray(
                        "required"
                )
                .add("description")
                .add("amount")
                .add("categoryName")
                .add("date")
                .add("type");

        return target;
    }

    private JsonNode buildChangesSchema() {
        var changes =
                objectMapper.createObjectNode();

        changes.put(
                "type",
                "object"
        );

        changes.put(
                "additionalProperties",
                false
        );

        ObjectNode properties =
                changes.putObject(
                        "properties"
                );

        properties.set(
                "description",
                buildStringChangeSchema()
        );

        properties.set(
                "amount",
                buildAmountChangeSchema()
        );

        properties.set(
                "categoryName",
                buildStringChangeSchema()
        );

        properties.set(
                "type",
                buildTypeChangeSchema()
        );

        changes.putArray(
                        "required"
                )
                .add("description")
                .add("amount")
                .add("categoryName")
                .add("type");

        return changes;
    }

    private JsonNode buildStringChangeSchema() {
        var change =
                objectMapper.createObjectNode();

        change.put(
                "type",
                "object"
        );

        change.put(
                "additionalProperties",
                false
        );

        ObjectNode properties =
                change.putObject(
                        "properties"
                );

        properties
                .putObject("specified")
                .put(
                        "type",
                        "boolean"
                );

        addNullableStringProperty(
                properties,
                "value"
        );

        change.putArray(
                        "required"
                )
                .add("specified")
                .add("value");

        return change;
    }

    private JsonNode buildAmountChangeSchema() {
        var change =
                objectMapper.createObjectNode();

        change.put(
                "type",
                "object"
        );

        change.put(
                "additionalProperties",
                false
        );

        ObjectNode properties =
                change.putObject(
                        "properties"
                );

        properties
                .putObject("specified")
                .put(
                        "type",
                        "boolean"
                );

        addNullableNumberProperty(
                properties,
                "value"
        );

        change.putArray(
                        "required"
                )
                .add("specified")
                .add("value");

        return change;
    }

    private JsonNode buildTypeChangeSchema() {
        return buildStringChangeSchema();
    }

    private void addNullableStringProperty(
            ObjectNode properties,
            String propertyName
    ) {
        properties
                .putObject(propertyName)
                .putArray("type")
                .add("string")
                .add("null");
    }

    private void addNullableNumberProperty(
            ObjectNode properties,
            String propertyName
    ) {
        properties
                .putObject(propertyName)
                .putArray("type")
                .add("number")
                .add("null");
    }
}