package com.puntomartinez.millete.assistant.infrastructure.out.verdict.client;

import com.puntomartinez.millete.assistant.infrastructure.out.verdict.config.VerdictProperties;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.dto.VerdictDecisionRequestDTO;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.dto.VerdictDecisionResponseDTO;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;

@Component
public class VerdictClient {

    private final RestClient restClient;
    private final String decider;

    public VerdictClient(VerdictProperties properties) {
        this.decider = properties.decider();
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isReady() {
        try {
            var response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .toEntity(JsonNode.class);
            JsonNode body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful()
                    || body == null || !"ok".equals(body.path("status").asText())) {
                return false;
            }

            JsonNode deciders = body.path("deciders");
            if (deciders.isArray()) {
                for (JsonNode entry : deciders) {
                    if (entry.isTextual() && decider.equals(entry.asText())) {
                        return true;
                    }
                }
            }
        } catch (RestClientException exception) {
            // Una comprobación fallida no debe impedir cargar el panel.
            return false;
        }
        return false;
    }

    public VerdictDecisionResponseDTO decide(
            VerdictDecisionRequestDTO request
    ) {
        return restClient
                .post()
                .uri("/v1/decide")
                .body(request)
                .retrieve()
                .body(VerdictDecisionResponseDTO.class);
    }
}
