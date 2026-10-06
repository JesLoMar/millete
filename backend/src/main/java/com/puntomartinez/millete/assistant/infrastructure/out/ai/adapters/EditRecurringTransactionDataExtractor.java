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
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditRecurringTransactionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers.EditRecurringTransactionExtractionMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class EditRecurringTransactionDataExtractor
        implements AiDataExtractor {

    private static final String SCHEMA_NAME =
            "edit_recurring_transaction";

    private static final String SYSTEM_PROMPT = """
        You extract structured data for the Millete personal finance application.

        The user wants to edit an existing recurring transaction.

        Your task has two parts:

        1. TARGET
        Identify the existing recurring transaction the user is referring to.

        2. CHANGES
        Extract only the fields the user explicitly wants to modify.

        TARGET FIELDS

        target.description
        - Concise description of the recurring transaction being referenced.
        - Example:
          "Edita la nómina recurrente y pon 2.200 €"
          -> "nómina"

        target.amount
        - Existing amount used to identify the recurring transaction.
        - Only provide it if the user explicitly uses the amount as part of the identification.
        - Do not confuse it with the new amount.
        - Example:
          "Cambia el recurrente de 50 € a 60 €"
          -> target.amount = 50
          -> changes.amount = 60

        target.categoryName
        - Category explicitly used to identify the recurring transaction.
        - Do not invent it.

        target.frequencyType
        - Existing recurrence unit used to identify the transaction.
        - Allowed values:
          DAYS, WEEKS, MONTHS, YEARS.

        target.frequencyInterval
        - Existing recurrence interval used to identify the transaction.
        - Only extract it when explicitly used to identify the target.

        target.type
        - Existing transaction type used to identify the target.
        - Allowed values:
          INCOME, EXPENSE.

        CHANGES

        changes.description
        - Set specified=true only when the user wants to change the description.
        - value contains the new description.

        changes.amount
        - Set specified=true only when the user wants to change the amount.
        - value contains the NEW amount.

        changes.type
        - Set specified=true only when the user wants to change the transaction type.
        - Allowed values:
          INCOME, EXPENSE.

        changes.frequencyType
        - Set specified=true only when the user wants to change the recurrence unit.
        - Allowed values:
          DAYS, WEEKS, MONTHS, YEARS.

        changes.frequencyInterval
        - Set specified=true only when the user wants to change the recurrence interval.
        - Example:
          "cada 2 semanas" -> frequencyType = WEEKS, frequencyInterval = 2
        - When the user changes frequency to a simple unit such as "mensual",
          "semanal", "diario" or "anual", use interval 1.

        IMPORTANT RULES

        - Target fields identify the existing recurring transaction.
        - Change fields describe the requested modifications.
        - Never put the NEW amount in target.amount.
        - Never put a requested new frequency in target.frequencyType.
        - Do not invent target criteria.
        - Do not invent changes.
        - If a target field is not explicitly available, return null.
        - Every change object must always be present.
        - Use specified=false and value=null for unchanged fields.
        - Never generate IDs.
        - Return only the JSON object matching the required schema.
        """;

    private final StructuredAiClient structuredAiClient;
    private final ObjectMapper objectMapper;

    public EditRecurringTransactionDataExtractor(
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
                != AppAction.EDIT_RECURRING_TRANSACTION) {

            throw new IllegalArgumentException(
                    "This extractor only supports EDIT_RECURRING_TRANSACTION"
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
                    "AI returned an empty recurring transaction edit response"
            );
        }

        try {
            var dto =
                    objectMapper.readValue(
                            rawResponse,
                            EditRecurringTransactionExtractionDTO.class
                    );

            return EditRecurringTransactionExtractionMapper.toDomain(
                    dto
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not parse AI recurring transaction edit response",
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
                "target",
                buildTargetSchema()
        );

        properties.set(
                "changes",
                buildChangesSchema()
        );

        schema.set(
                "properties",
                properties
        );

        var required =
                objectMapper.createArrayNode();

        required.add("target");
        required.add("changes");

        schema.set(
                "required",
                required
        );

        return schema;
    }

    private JsonNode buildTargetSchema() {
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
                "description",
                nullableStringSchema()
        );

        properties.set(
                "amount",
                nullableNumberSchema()
        );

        properties.set(
                "categoryName",
                nullableStringSchema()
        );

        ObjectNode frequencyType =
                nullableStringSchema();

        frequencyType.set(
                "enum",
                buildFrequencyTypeEnum()
        );

        properties.set(
                "frequencyType",
                frequencyType
        );

        properties.set(
                "frequencyInterval",
                nullableIntegerSchema()
        );

        ObjectNode type =
                nullableStringSchema();

        type.set(
                "enum",
                buildTransactionTypeEnum()
        );

        properties.set(
                "type",
                type
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
        required.add("frequencyType");
        required.add("frequencyInterval");
        required.add("type");

        schema.set(
                "required",
                required
        );

        return schema;
    }

    private JsonNode buildChangesSchema() {
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
                "description",
                buildFieldChangeSchema(
                        "string"
                )
        );

        properties.set(
                "amount",
                buildFieldChangeSchema(
                        "number"
                )
        );

        properties.set(
                "type",
                buildFieldChangeSchema(
                        "string"
                )
        );

        properties.set(
                "frequencyType",
                buildFieldChangeSchema(
                        "string"
                )
        );

        properties.set(
                "frequencyInterval",
                buildFieldChangeSchema(
                        "integer"
                )
        );

        schema.set(
                "properties",
                properties
        );

        var required =
                objectMapper.createArrayNode();

        required.add("description");
        required.add("amount");
        required.add("type");
        required.add("frequencyType");
        required.add("frequencyInterval");

        schema.set(
                "required",
                required
        );

        return schema;
    }

    private JsonNode buildFieldChangeSchema(
            String valueType
    ) {
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

        var specified =
                objectMapper.createObjectNode();

        specified.put(
                "type",
                "boolean"
        );

        properties.set(
                "specified",
                specified
        );

        var value =
                objectMapper.createObjectNode();

        var valueTypes =
                objectMapper.createArrayNode();

        valueTypes.add(
                valueType
        );

        valueTypes.add(
                "null"
        );

        value.set(
                "type",
                valueTypes
        );

        properties.set(
                "value",
                value
        );

        schema.set(
                "properties",
                properties
        );

        var required =
                objectMapper.createArrayNode();

        required.add("specified");
        required.add("value");

        schema.set(
                "required",
                required
        );

        return schema;
    }

    private ObjectNode nullableStringSchema() {
        var schema =
                objectMapper.createObjectNode();

        var types =
                objectMapper.createArrayNode();

        types.add("string");
        types.add("null");

        schema.set(
                "type",
                types
        );

        return schema;
    }

    private JsonNode nullableNumberSchema() {
        var schema =
                objectMapper.createObjectNode();

        var types =
                objectMapper.createArrayNode();

        types.add("number");
        types.add("null");

        schema.set(
                "type",
                types
        );

        return schema;
    }

    private JsonNode nullableIntegerSchema() {
        var schema =
                objectMapper.createObjectNode();

        var types =
                objectMapper.createArrayNode();

        types.add("integer");
        types.add("null");

        schema.set(
                "type",
                types
        );

        return schema;
    }

    private JsonNode buildFrequencyTypeEnum() {
        var values =
                objectMapper.createArrayNode();

        values.add("DAYS");
        values.add("WEEKS");
        values.add("MONTHS");
        values.add("YEARS");
        values.addNull();

        return values;
    }

    private JsonNode buildTransactionTypeEnum() {
        var values =
                objectMapper.createArrayNode();

        values.add("INCOME");
        values.add("EXPENSE");
        values.addNull();

        return values;
    }
}