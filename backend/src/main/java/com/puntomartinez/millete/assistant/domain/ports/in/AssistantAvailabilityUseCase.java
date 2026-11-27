package com.puntomartinez.millete.assistant.domain.ports.in;

@FunctionalInterface
public interface AssistantAvailabilityUseCase {
    boolean isAvailable();
}
