package com.puntomartinez.millete.assistant.infrastructure.out.ai.dto;

import java.math.BigDecimal;

public record AddRecurringTransactionExtractionDTO(
        String description,
        BigDecimal amount,
        String categoryName,
        String frequencyType,
        Integer frequencyInterval,
        String startDate,
        String endDate
) {
}