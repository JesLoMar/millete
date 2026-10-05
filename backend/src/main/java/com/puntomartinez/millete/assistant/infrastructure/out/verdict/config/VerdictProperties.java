package com.puntomartinez.millete.assistant.infrastructure.out.verdict.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "millete.ai.verdict")
public record VerdictProperties(
String baseUrl,
String decider,
Duration connectTimeout,
Duration readTimeout
) {

public VerdictProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
        throw new IllegalArgumentException("Verdict baseUrl cannot be blank");
    }

    if (decider == null || decider.isBlank()) {
        throw new IllegalArgumentException("Verdict decider cannot be blank");
    }

    if (connectTimeout == null) {
        connectTimeout = Duration.ofSeconds(2);
    }

    if (readTimeout == null) {
        readTimeout = Duration.ofSeconds(5);
    }
}

}