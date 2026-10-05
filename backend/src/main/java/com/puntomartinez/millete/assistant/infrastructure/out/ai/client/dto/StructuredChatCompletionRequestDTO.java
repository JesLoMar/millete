package com.puntomartinez.millete.assistant.infrastructure.out.ai.client.dto;

import java.util.List;

public record StructuredChatCompletionRequestDTO(
        String model,
        List<MessageDTO> messages,
        ResponseFormatDTO response_format
) {

    public record MessageDTO(
            String role,
            String content
    ) {
    }

    public record ResponseFormatDTO(
            String type
    ) {
    }
}