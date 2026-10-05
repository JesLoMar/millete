package com.puntomartinez.millete.assistant.infrastructure.out.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "millete.ai")
public record AiProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        Duration connectTimeout,
        Duration readTimeout
) {

    public AiProperties {
        if (enabled) {
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException(
                        "AI baseUrl cannot be blank when AI is enabled"
                );
            }

            if (model == null || model.isBlank()) {
                throw new IllegalArgumentException(
                        "AI model cannot be blank when AI is enabled"
                );
            }
        }

        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(2);
        }

        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(15);
        }

        if (apiKey == null) {
            apiKey = "";
        }
    }
}