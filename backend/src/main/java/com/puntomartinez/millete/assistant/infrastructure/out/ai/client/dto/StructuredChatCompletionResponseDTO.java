package com.puntomartinez.millete.assistant.infrastructure.out.ai.client.dto;

import java.util.List;

public record StructuredChatCompletionResponseDTO(
        List<ChoiceDTO> choices
) {

    public record ChoiceDTO(
            MessageDTO message,
            String finish_reason
    ) {
    }

    public record MessageDTO(
            String role,
            String content
    ) {
    }
}