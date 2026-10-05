package com.puntomartinez.millete.assistant.infrastructure.out.ai.dto;

import java.math.BigDecimal;

public record AddCategoryExtractionDTO(
        String name,
        String description,
        BigDecimal budgetLimit
) {
}