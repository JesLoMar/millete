package com.puntomartinez.millete.assistant.infrastructure.out.ai.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddCategoryData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiClient;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.StructuredAiRequest;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddCategoryExtractionDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers.AddCategoryExtractionMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class AddCategoryDataExtractor
        implements AiDataExtractor {

    private static final String SCHEMA_NAME = "add_category";

    private static final String SYSTEM_PROMPT = """
            You extract data for the Millete personal finance application.

            The user's input may be written in any language.

            Extract only the information explicitly provided by the user
            for creating a new category.

            Rules:
            - Extract the category name if the user explicitly provides it.
            - Extract the description only if the user explicitly provides one.
            - Extract the budget limit only if the user explicitly provides one.
            - If a value was not provided, return null.
            - Never invent or guess a category name.
            - Never invent or guess a description.
            - Never invent or guess a budget limit.
            - Do not extract or generate a color.
            - Do not extract or generate IDs.
            - Do not add any fields outside the schema.
            - Preserve the meaning of the user's words.
            - Return only the JSON object matching the required schema.
            """;

    private final StructuredAiClient structuredAiClient;
    private final ObjectMapper objectMapper;

    public AddCategoryDataExtractor(
            StructuredAiClient structuredAiClient,
            ObjectMapper objectMapper
    ) {
        this.structuredAiClient = Objects.requireNonNull(
                structuredAiClient,
                "structuredAiClient cannot be null"
        );

        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper cannot be null"
        );
    }

    @Override
    public InterpretationData extract(AiExtractionContext context) {
        Objects.requireNonNull(context, "context cannot be null");

        if (context.action() != AppAction.ADD_CATEGORY) {
            throw new IllegalArgumentException(
                    "This extractor only supports ADD_CATEGORY"
            );
        }

        var request = new StructuredAiRequest(
                SYSTEM_PROMPT,
                context.input(),
                SCHEMA_NAME,
                buildResponseSchema()
        );

        var rawResponse = structuredAiClient.generate(request);

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException(
                    "AI returned an empty extraction response"
            );
        }

        try {
            var dto = objectMapper.readValue(
                    rawResponse,
                    AddCategoryExtractionDTO.class
            );

            return AddCategoryExtractionMapper.toDomain(dto);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not parse AI category extraction response",
                    exception
            );
        }
    }

    private JsonNode buildResponseSchema() {
        var schema = objectMapper.createObjectNode();

        schema.put("type", "object");
        schema.put("additionalProperties", false);

        var properties = objectMapper.createObjectNode();

        /*
         * name is nullable because the user may not have
         * provided the mandatory information yet.
         */
        var name = objectMapper.createObjectNode();
        var nameTypes = objectMapper.createArrayNode();
        nameTypes.add("string");
        nameTypes.add("null");
        name.set("type", nameTypes);
        properties.set("name", name);

        var description = objectMapper.createObjectNode();
        var descriptionTypes = objectMapper.createArrayNode();
        descriptionTypes.add("string");
        descriptionTypes.add("null");
        description.set("type", descriptionTypes);
        properties.set("description", description);

        var budgetLimit = objectMapper.createObjectNode();
        var budgetTypes = objectMapper.createArrayNode();
        budgetTypes.add("number");
        budgetTypes.add("null");
        budgetLimit.set("type", budgetTypes);
        properties.set("budgetLimit", budgetLimit);

        schema.set("properties", properties);

        /*
         * The keys are always present in the JSON.
         * Their values may be null when the information
         * was not provided by the user.
         */
        var required = objectMapper.createArrayNode();
        required.add("name");
        required.add("description");
        required.add("budgetLimit");

        schema.set("required", required);

        return schema;
    }
}