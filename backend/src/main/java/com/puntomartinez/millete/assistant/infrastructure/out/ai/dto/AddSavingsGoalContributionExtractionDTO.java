package com.puntomartinez.millete.assistant.infrastructure.out.ai.dto;

import java.math.BigDecimal;

public record AddSavingsGoalContributionExtractionDTO(
        String targetName,
        BigDecimal amount
) {
}