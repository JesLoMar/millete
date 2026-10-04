package com.puntomartinez.millete.assistant.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record InterpretUserInputRequestDTO(
        @NotBlank(message = "Input cannot be blank")
        String input
) {
}