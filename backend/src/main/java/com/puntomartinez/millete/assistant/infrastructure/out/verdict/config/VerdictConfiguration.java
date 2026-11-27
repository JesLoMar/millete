package com.puntomartinez.millete.assistant.infrastructure.out.verdict.config;

import com.puntomartinez.millete.assistant.domain.ports.in.AssistantAvailabilityUseCase;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.config.AiProperties;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.client.VerdictClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(VerdictProperties.class)
public class VerdictConfiguration {

    @Bean
    AssistantAvailabilityUseCase assistantAvailability(AiProperties ai, VerdictClient verdict) {
        return () -> ai.enabled() && verdict.isReady();
    }
}
