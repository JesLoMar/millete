package com.puntomartinez.millete.assistant.infrastructure.out.ai.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiClient;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiRequest;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddRecurringTransactionExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers.AddRecurringTransactionExtractionMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class AddRecurringTransactionDataExtractor
        implements AiDataExtractor {

    private static final String SCHEMA_NAME =
            "add_recurring_transaction";

    private static final String SYSTEM_PROMPT = """
        You extract data for the Millete personal finance application.

        The user's input may be written in any language.

        Extract information for creating a new recurring transaction.

        The action determines whether the transaction is an expense or an income.
        Do not extract or generate a transaction type.

        Extract these fields:

        1. description
        - A short description of what the recurring transaction is about.
        - Do NOT copy the whole user sentence.
        - Prefer the concise concept or reason of the transaction.

        Examples:
        "Añade un gasto mensual de 50 € de Netflix"
        -> description: "Netflix"

        "Cada mes recibo 800 € de alquiler"
        -> description: "Alquiler"

        2. amount
        - The recurring transaction amount.
        - Return only the numeric amount.
        - Do not include currency symbols.

        Examples:
        "50 €" -> 50
        "800 euros" -> 800

        3. categoryName
        - The category explicitly mentioned by the user.
        - Do not invent a category.
        - Do not infer a category from the description.
        - If no category is explicitly provided, return null.

        4. frequencyType
        - The recurrence unit.
        - Allowed values are exactly:
          DAYS, WEEKS, MONTHS, YEARS.

        Examples:
        "cada día" -> DAYS
        "diario" -> DAYS
        "cada semana" -> WEEKS
        "semanal" -> WEEKS
        "cada mes" -> MONTHS
        "mensual" -> MONTHS
        "cada año" -> YEARS
        "anual" -> YEARS

        - If no recurrence unit is provided, return null.

        5. frequencyInterval
        - The numeric recurrence interval.

        Examples:
        "cada 2 semanas" -> 2
        "cada 3 meses" -> 3
        "cada mes" -> 1
        "mensual" -> 1

        - If a recurrence unit is explicitly provided but no number is provided,
          return 1.
        - If no recurrence unit is provided, return null.

        6. startDate
        - Extract an explicitly provided calendar date.
        - Return it using ISO format YYYY-MM-DD.
        - If the user does not explicitly provide a start date, return null.
        - Do not invent a date.
        - Do not use today's date unless the user explicitly says today.

        7. endDate
        - Extract an explicitly provided end date.
        - Return it using ISO format YYYY-MM-DD.
        - If no end date is provided, return null.
        - Do not invent an end date.

        Important rules:
        - Extract only information supported by the user's input.
        - Never invent description, amount or categoryName.
        - Never invent a frequency.
        - Never invent dates.
        - Do not generate IDs.
        - Do not generate transaction type.
        - The action already determines whether the transaction is an expense or income.
        - Always return every field required by the schema.
        - Use null when information is not available.
        - Return only the JSON object matching the required schema.
        """;

    private final StructuredAiClient structuredAiClient;
    private final ObjectMapper objectMapper;

    public AddRecurringTransactionDataExtractor(
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
                    "AI returned an empty recurring transaction extraction response"
            );
        }

        try {
            var dto =
                    objectMapper.readValue(
                            rawResponse,
                            AddRecurringTransactionExtractionDTO.class
                    );

            return AddRecurringTransactionExtractionMapper.toDomain(
                    dto
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Could not parse AI recurring transaction extraction response",
                    exception
            );
        }
    }

    private void validateAction(
            AppAction action
    ) {
        if (action != AppAction.ADD_RECURRING_EXPENSE_TRANSACTION
                && action != AppAction.ADD_RECURRING_INCOME_TRANSACTION) {

            throw new IllegalArgumentException(
                    "This extractor only supports "
                            + "ADD_RECURRING_EXPENSE_TRANSACTION and "
                            + "ADD_RECURRING_INCOME_TRANSACTION"
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
                "description",
                nullableStringSchema()
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

        properties.set(
                "categoryName",
                nullableStringSchema()
        );

        var frequencyType =
                nullableStringSchema();

        frequencyType.set(
                "enum",
                buildFrequencyEnum()
        );

        properties.set(
                "frequencyType",
                frequencyType
        );

        var frequencyInterval =
                objectMapper.createObjectNode();

        var intervalTypes =
                objectMapper.createArrayNode();

        intervalTypes.add("integer");
        intervalTypes.add("null");

        frequencyInterval.set(
                "type",
                intervalTypes
        );

        properties.set(
                "frequencyInterval",
                frequencyInterval
        );

        properties.set(
                "startDate",
                nullableStringSchema()
        );

        properties.set(
                "endDate",
                nullableStringSchema()
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
        required.add("startDate");
        required.add("endDate");

        schema.set(
                "required",
                required
        );

        return schema;
    }

    private JsonNode nullableStringSchema() {
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

    private JsonNode buildFrequencyEnum() {
        var values =
                objectMapper.createArrayNode();

        values.add("DAYS");
        values.add("WEEKS");
        values.add("MONTHS");
        values.add("YEARS");
        values.addNull();

        return values;
    }
}