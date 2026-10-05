package com.puntomartinez.millete.assistant.infrastructure.out.ai.dto;

import java.math.BigDecimal;

public record AddTransactionExtractionDTO(
        String description,
        BigDecimal amount,
        String categoryName
) {
}