package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddRecurringTransactionData(
        String description,
        BigDecimal amount,
        String categoryName,
        FrequencyType frequencyType,
        Integer frequencyInterval,
        LocalDate startDate,
        LocalDate endDate
) implements InterpretationData {
}