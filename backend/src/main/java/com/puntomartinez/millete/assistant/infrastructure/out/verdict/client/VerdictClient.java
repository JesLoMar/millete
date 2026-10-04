package com.millete.assistant.infrastructure.out.verdict.client;

import com.millete.assistant.infrastructure.out.verdict.config.VerdictProperties;
import com.millete.assistant.infrastructure.out.verdict.dto.VerdictDecisionRequestDTO;
import com.millete.assistant.infrastructure.out.verdict.dto.VerdictDecisionResponseDTO;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class VerdictClient {

    private final RestClient restClient;

    public VerdictClient(VerdictProperties properties) {
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