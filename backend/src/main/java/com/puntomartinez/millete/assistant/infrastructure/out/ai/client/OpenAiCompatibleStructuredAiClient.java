package com.puntomartinez.millete.assistant.infrastructure.out.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.dto.StructuredChatCompletionRequestDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.client.dto.StructuredChatCompletionResponseDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.config.AiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Objects;

@Component
@ConditionalOnProperty(
        prefix = "millete.ai",
        name = "enabled",
        havingValue = "true"
)
public class OpenAiCompatibleStructuredAiClient
        implements StructuredAiClient {

    private static final String CHAT_COMPLETIONS_PATH =
            "chat/completions";

    private static final String JSON_RESPONSE_TYPE =
            "json_object";

    private static final String SCHEMA_INSTRUCTIONS = """
            The response must be a valid JSON object.

            The JSON object must follow this JSON Schema exactly:

            %s

            Do not include any field that is not present in the schema.
            """;

    private final RestClient restClient;
    private final AiProperties properties;

    public OpenAiCompatibleStructuredAiClient(
            AiProperties properties,
            ObjectMapper objectMapper
    ) {
        Objects.requireNonNull(
                objectMapper,
                "objectMapper cannot be null"
        );

        this.properties = Objects.requireNonNull(
                properties,
                "properties cannot be null"
        );

        var httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();

        var requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(
                properties.readTimeout()
        );

        this.restClient = RestClient.builder()
                .baseUrl(
                        normalizeBaseUrl(
                                properties.baseUrl()
                        )
                )
                .requestFactory(requestFactory)
                .defaultHeader(
                        "Content-Type",
                        MediaType.APPLICATION_JSON_VALUE
                )
                .defaultHeaders(headers -> {
                    if (!properties.apiKey().isBlank()) {
                        headers.setBearerAuth(
                                properties.apiKey()
                        );
                    }
                })
                .build();
    }

    @Override
    public String generate(
            StructuredAiRequest request
    ) {
        Objects.requireNonNull(
                request,
                "request cannot be null"
        );

        var systemPrompt = buildSystemPrompt(request);

        var body = new StructuredChatCompletionRequestDTO(
                properties.model(),
                List.of(
                        new StructuredChatCompletionRequestDTO.MessageDTO(
                                "system",
                                systemPrompt
                        ),
                        new StructuredChatCompletionRequestDTO.MessageDTO(
                                "user",
                                request.userPrompt()
                        )
                ),
                new StructuredChatCompletionRequestDTO.ResponseFormatDTO(
                        JSON_RESPONSE_TYPE
                )
        );

        try {
            var response = restClient
                    .post()
                    .uri(CHAT_COMPLETIONS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(
                            StructuredChatCompletionResponseDTO.class
                    );

            return extractContent(response);

        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Structured AI request failed",
                    exception
            );
        }
    }

    private static String buildSystemPrompt(
            StructuredAiRequest request
    ) {
        return """
                %s

                %s

                Schema name: %s
                """.formatted(
                request.systemPrompt(),
                SCHEMA_INSTRUCTIONS.formatted(
                        request.responseSchema().toPrettyString()
                ),
                request.schemaName()
        );
    }

    private static String extractContent(
            StructuredChatCompletionResponseDTO response
    ) {
        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()) {
            throw new IllegalStateException(
                    "Structured AI response contains no choices"
            );
        }

        var choice = response.choices().getFirst();

        if ("length".equals(choice.finish_reason())) {
            throw new IllegalStateException(
                    "Structured AI response was truncated"
            );
        }

        if (choice.message() == null
                || choice.message().content() == null
                || choice.message().content().isBlank()) {
            throw new IllegalStateException(
                    "Structured AI response contains no content"
            );
        }

        return choice.message().content();
    }

    private static String normalizeBaseUrl(
            String baseUrl
    ) {
        var normalized = baseUrl.trim();

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        return normalized + "/";
    }
}