package com.puntomartinez.millete.assistant.infrastructure.out.verdict.adapters;

import com.puntomartinez.millete.assistant.domain.model.interpretation.EditTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionCandidate;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionResolutionStatus;
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
public final class VerdictTransactionSelector {

    private static final String QUESTION_ID =
            "transaction";

    private static final String NONE_OF_THE_ABOVE =
            "none_of_the_above";

    private static final double MIN_CONFIDENCE =
            0.70;

    private static final double MIN_MARGIN =
            0.15;

    private final RestClient restClient;

    public VerdictTransactionSelector(
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

    public TransactionResolution select(
            EditTransactionData.TransactionTarget target,
            List<TransactionCandidate> candidates
    ) {
        Objects.requireNonNull(
                target,
                "target cannot be null"
        );

        Objects.requireNonNull(
                candidates,
                "candidates cannot be null"
        );

        if (!target.hasCriteria()) {
            throw new IllegalArgumentException(
                    "Transaction target must contain at least one criterion"
            );
        }

        if (candidates.isEmpty()) {
            return TransactionResolution.notFound(
                    buildReference(target),
                    0.0,
                    0.0
            );
        }

        var request =
                buildRequest(
                        target,
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
                    "Verdict transaction selection request failed",
                    exception
            );
        }

        return parseResponse(
                target,
                candidates,
                response
        );
    }

    private Map<String, Object> buildRequest(
            EditTransactionData.TransactionTarget target,
            List<TransactionCandidate> candidates
    ) {
        var state =
                new LinkedHashMap<String, Object>();

        state.put(
                "transactionReference",
                buildReference(target)
        );

        var availableTransactions =
                candidates.stream()
                        .map(candidate -> {
                            var transaction =
                                    new LinkedHashMap<String, Object>();

                            transaction.put(
                                    "option",
                                    optionKey(
                                            candidates,
                                            candidate
                                    )
                            );

                            transaction.put(
                                    "description",
                                    candidate.description()
                            );

                            transaction.put(
                                    "amount",
                                    candidate.amount()
                            );

                            transaction.put(
                                    "categoryName",
                                    candidate.categoryName()
                            );

                            transaction.put(
                                    "date",
                                    candidate.date().toString()
                            );

                            transaction.put(
                                    "type",
                                    candidate.type().name()
                            );

                            return transaction;
                        })
                        .toList();

        state.put(
                "availableTransactions",
                availableTransactions
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
                    This option represents one existing Millete transaction.

                    Description: "%s"
                    Amount: %s
                    Category: %s
                    Date: %s
                    Type: %s

                    Prefer this transaction when these details best match
                    the user's target reference.
                    """.formatted(
                            candidate.description(),
                            candidate.amount(),
                            candidate.categoryName() == null
                                    ? "no category"
                                    : candidate.categoryName(),
                            candidate.date(),
                            candidate.type()
                    )
            );
        }

        criteria.put(
                NONE_OF_THE_ABOVE,
                """
                Select this option when none of the available transactions
                is a sufficiently good match for the user's target.

                Do not select a transaction merely because it is vaguely
                similar. The identifying details should provide a reasonable
                match.
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
                Select the existing Millete transaction that best matches
                the user's target reference.

                Compare the identifying details semantically and
                contextually.

                Consider:
                - description;
                - amount;
                - category;
                - date;
                - transaction type.

                A natural-language reference does not need to use the exact
                wording stored in the transaction.

                Prefer the most specific matching transaction.
                If no transaction is a reasonable match, choose
                "none_of_the_above".
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

    private TransactionResolution parseResponse(
            EditTransactionData.TransactionTarget target,
            List<TransactionCandidate> candidates,
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
                    "Verdict response contains no transaction answer"
            );
        }

        var choice =
                answer
                        .path("choice")
                        .asText(null);

        if (choice == null
                || choice.isBlank()) {

            throw new IllegalStateException(
                    "Verdict transaction answer contains no choice"
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
                    "Verdict transaction answer contains no valid confidence"
            );
        }

        var probabilities =
                answer.path("probabilities");

        if (!probabilities.isObject()) {
            throw new IllegalStateException(
                    "Verdict transaction answer contains no probabilities"
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

        var reference =
                buildReference(target);

        if (NONE_OF_THE_ABOVE.equals(choice)) {
            return TransactionResolution.notFound(
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
                    "Verdict selected an unknown transaction option: "
                            + choice
            );
        }

        if (confidence < MIN_CONFIDENCE
                || margin < MIN_MARGIN) {

            return TransactionResolution.ambiguous(
                    reference,
                    confidence,
                    margin
            );
        }

        return TransactionResolution.found(
                reference,
                selectedCandidate,
                confidence,
                margin
        );
    }

    private TransactionCandidate resolveCandidate(
            String choice,
            List<TransactionCandidate> candidates
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
            List<TransactionCandidate> candidates,
            TransactionCandidate candidate
    ) {
        var index =
                candidates.indexOf(candidate);

        if (index < 0) {
            throw new IllegalArgumentException(
                    "Candidate does not belong to candidate list"
            );
        }

        return "transaction_" + (index + 1);
    }

    private static String buildReference(
            EditTransactionData.TransactionTarget target
    ) {
        var parts =
                new LinkedHashMap<String, String>();

        if (target.description() != null) {
            parts.put(
                    "description",
                    target.description()
            );
        }

        if (target.amount() != null) {
            parts.put(
                    "amount",
                    target.amount().toString()
            );
        }

        if (target.categoryName() != null) {
            parts.put(
                    "category",
                    target.categoryName()
            );
        }

        if (target.date() != null) {
            parts.put(
                    "date",
                    target.date().toString()
            );
        }

        if (target.type() != null) {
            parts.put(
                    "type",
                    target.type().name()
            );
        }

        return parts.entrySet()
                .stream()
                .map(entry ->
                        entry.getKey()
                                + "="
                                + entry.getValue()
                )
                .reduce(
                        (left, right) ->
                                left + ", " + right
                )
                .orElse(
                        "transaction"
                );
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