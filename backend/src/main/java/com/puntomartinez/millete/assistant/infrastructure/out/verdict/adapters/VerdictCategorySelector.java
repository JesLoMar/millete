package com.puntomartinez.millete.assistant.infrastructure.out.verdict.adapters;

import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryCandidate;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolutionStatus;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.config.VerdictProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public final class VerdictCategorySelector {

    private static final String QUESTION_ID =
            "category";

    private static final String NONE_OF_THE_ABOVE =
            "none_of_the_above";

    private static final double MIN_CONFIDENCE =
            0.70;

    private static final double MIN_MARGIN =
            0.15;

    private final RestClient restClient;

    public VerdictCategorySelector(
            VerdictProperties properties
    ) {
        Objects.requireNonNull(
                properties,
                "properties cannot be null"
        );

        var httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                properties.connectTimeout()
                        )
                        .build();

        var requestFactory =
                new JdkClientHttpRequestFactory(
                        httpClient
                );

        requestFactory.setReadTimeout(
                properties.readTimeout()
        );

        this.restClient =
                RestClient.builder()
                        .baseUrl(
                                normalizeBaseUrl(
                                        properties.baseUrl()
                                )
                        )
                        .requestFactory(
                                requestFactory
                        )
                        .defaultHeader(
                                "Content-Type",
                                MediaType.APPLICATION_JSON_VALUE
                        )
                        .build();
    }

    public CategoryResolution select(
            String reference,
            List<CategoryCandidate> candidates
    ) {
        Objects.requireNonNull(
                reference,
                "reference cannot be null"
        );

        Objects.requireNonNull(
                candidates,
                "candidates cannot be null"
        );

        if (reference.isBlank()) {
            throw new IllegalArgumentException(
                    "reference cannot be blank"
            );
        }

        if (candidates.isEmpty()) {
            return CategoryResolution.notFound(
                    reference,
                    0.0,
                    0.0
            );
        }

        var request =
                buildRequest(
                        reference,
                        candidates
                );

        JsonNode response;

        try {
            response =
                    restClient
                            .post()
                            .uri("v1/systemone")
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .body(request)
                            .retrieve()
                            .body(JsonNode.class);

        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Verdict category selection request failed",
                    exception
            );
        }

        return parseResponse(
                reference,
                candidates,
                response
        );
    }

    private Map<String, Object> buildRequest(
            String reference,
            List<CategoryCandidate> candidates
    ) {
        var state =
                new LinkedHashMap<String, Object>();

        state.put(
                "categoryReference",
                reference
        );

        state.put(
                "availableCategories",
                candidates.stream()
                        .map(candidate ->
                                Map.of(
                                        "option",
                                        optionKey(
                                                candidates,
                                                candidate
                                        ),
                                        "name",
                                        candidate.name()
                                )
                        )
                        .toList()
        );

        var criteria =
                new LinkedHashMap<String, String>();

        for (var candidate : candidates) {
            criteria.put(
                    optionKey(
                            candidates,
                            candidate
                    ),
                    """
                    Select this category when its meaning is the closest
                    match to the user's category reference.

                    Compare the user's reference with the category name
                    semantically, not only textually.

                    Consider:
                    - synonyms;
                    - common alternative terms;
                    - colloquial expressions;
                    - abbreviations;
                    - natural language variations;
                    - concepts that refer to the same thing.

                    Examples:
                    - "nómina" can refer to "Sueldo".
                    - "salario" can refer to "Sueldo".
                    - "payroll" can refer to "Sueldo".
                    - "gasolina" can refer to "Gasolina".

                    Choose this category only when its meaning is a good
                    match for the user's reference.
                    """.formatted(
                            candidate.name()
                    )
            );
        }

        criteria.put(
                NONE_OF_THE_ABOVE,
                """
                Select this option when none of the available categories
                is a sufficiently good semantic match for the user's
                category reference.

                Do not choose a category only because it belongs to the
                same broad financial type. The category must be relevant
                to the specific meaning of the user's reference.
                """
        );

        var question =
                new LinkedHashMap<String, Object>();

        question.put(
                "type",
                "choice"
        );

        question.put(
                "instructions",
                """
                Choose the available Millete category that best matches
                the user's category reference semantically.

                The user's reference may not exactly match the category
                name. It may use a synonym, colloquial expression,
                abbreviation, or another natural-language expression.

                Prefer the category with the closest meaning.

                Example:
                - User reference: "nómina"
                - "Sueldo" is a better match than "Ingreso extra".

                Another example:
                - User reference: "gasolina"
                - "Gasolina" is a better match than "Transporte".

                Choose "none_of_the_above" only when no available category
                is a reasonable semantic match.
                """
        );

        question.put(
                "criteria",
                criteria
        );

        return Map.of(
                "state",
                state,
                "questions",
                Map.of(
                        QUESTION_ID,
                        question
                )
        );
    }

    private CategoryResolution parseResponse(
            String reference,
            List<CategoryCandidate> candidates,
            JsonNode response
    ) {
        if (response == null) {
            throw new IllegalStateException(
                    "Verdict returned an empty response"
            );
        }

        var answer =
                response
                        .path("answers")
                        .path(QUESTION_ID);

        if (answer.isMissingNode()) {
            throw new IllegalStateException(
                    "Verdict response contains no category answer"
            );
        }

        var choice =
                answer
                        .path("choice")
                        .asText(null);

        if (choice == null
                || choice.isBlank()) {

            throw new IllegalStateException(
                    "Verdict category answer contains no choice"
            );
        }

        var confidence =
                answer
                        .path("confidence")
                        .asDouble(
                                Double.NaN
                        );

        if (!Double.isFinite(confidence)) {
            throw new IllegalStateException(
                    "Verdict category answer contains no valid confidence"
            );
        }

        var probabilities =
                answer.path("probabilities");

        if (!probabilities.isObject()) {
            throw new IllegalStateException(
                    "Verdict category answer contains no probabilities"
            );
        }

        var orderedProbabilities =
                new LinkedHashMap<String, Double>();

        probabilities
                .propertyStream()
                .forEach(entry -> {
                    var value =
                            entry.getValue()
                                    .asDouble(
                                            Double.NaN
                                    );

                    if (Double.isFinite(value)) {
                        orderedProbabilities.put(
                                entry.getKey(),
                                value
                        );
                    }
                });

        var topTwo =
                orderedProbabilities
                        .values()
                        .stream()
                        .sorted(
                                Comparator.reverseOrder()
                        )
                        .limit(2)
                        .toList();

        var topProbability =
                topTwo.isEmpty()
                        ? 0.0
                        : topTwo.getFirst();

        var secondProbability =
                topTwo.size() < 2
                        ? 0.0
                        : topTwo.get(1);

        var margin =
                topProbability
                        - secondProbability;

        if (NONE_OF_THE_ABOVE.equals(choice)) {
            return CategoryResolution.notFound(
                    reference,
                    confidence,
                    margin
            );
        }

        var selectedCandidate =
                resolveCandidate(
                        choice,
                        candidates
                );

        if (selectedCandidate == null) {
            throw new IllegalStateException(
                    "Verdict selected an unknown category option: "
                            + choice
            );
        }

        if (confidence < MIN_CONFIDENCE
                || margin < MIN_MARGIN) {

            return CategoryResolution.ambiguous(
                    reference,
                    confidence,
                    margin
            );
        }

        return CategoryResolution.found(
                reference,
                selectedCandidate,
                confidence,
                margin
        );
    }

    private CategoryCandidate resolveCandidate(
            String choice,
            List<CategoryCandidate> candidates
    ) {
        for (int i = 0;
             i < candidates.size();
             i++) {

            var candidate =
                    candidates.get(i);

            if (optionKey(
                    candidates,
                    candidate
            ).equals(choice)) {

                return candidate;
            }
        }

        return null;
    }

    private String optionKey(
            List<CategoryCandidate> candidates,
            CategoryCandidate candidate
    ) {
        var index =
                candidates.indexOf(candidate);

        if (index < 0) {
            throw new IllegalArgumentException(
                    "Candidate does not belong to candidate list"
            );
        }

        return "category_" + (index + 1);
    }

    private static String normalizeBaseUrl(
            String baseUrl
    ) {
        var normalized =
                Objects.requireNonNull(
                        baseUrl,
                        "baseUrl cannot be null"
                ).trim();

        while (normalized.endsWith("/")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        return normalized + "/";
    }
}