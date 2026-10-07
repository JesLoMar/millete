package com.puntomartinez.millete.assistant.infrastructure.out.ai.dto;

import java.math.BigDecimal;

public record EditSavingsGoalExtractionDTO(
        String targetName,
        String name,
        BigDecimal targetAmount,
        String priority,
        String deadline,
        String link
) {
}