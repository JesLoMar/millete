package com.puntomartinez.millete.assistant.infrastructure.out.verdict.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(VerdictProperties.class)
public class VerdictConfiguration {
}