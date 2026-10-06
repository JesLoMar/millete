package com.puntomartinez.millete.assistant.infrastructure.out.ai.dto;

import java.math.BigDecimal;

public record EditRecurringTransactionExtractionDTO(
        TargetDTO target,
        ChangesDTO changes
) {

    public record TargetDTO(
            String description,
            BigDecimal amount,
            String categoryName,
            String frequencyType,
            Integer frequencyInterval,
            String type
    ) {
    }

    public record ChangesDTO(
            FieldChangeDTO<String> description,
            FieldChangeDTO<BigDecimal> amount,
            FieldChangeDTO<String> type,
            FieldChangeDTO<String> frequencyType,
            FieldChangeDTO<Integer> frequencyInterval
    ) {
    }

    public record FieldChangeDTO<T>(
            Boolean specified,
            T value
    ) {
    }
}