package com.puntomartinez.millete.assistant.infrastructure.out.ai.dto;

import java.math.BigDecimal;

public record EditTransactionExtractionDTO(
        TargetDTO target,
        ChangesDTO changes
) {

    public record TargetDTO(
            String description,
            BigDecimal amount,
            String categoryName,
            String date,
            String type
    ) {
    }

    public record ChangesDTO(
            StringChangeDTO description,
            AmountChangeDTO amount,
            StringChangeDTO categoryName,
            TypeChangeDTO type
    ) {
    }

    public record StringChangeDTO(
            boolean specified,
            String value
    ) {
    }

    public record AmountChangeDTO(
            boolean specified,
            BigDecimal value
    ) {
    }

    public record TypeChangeDTO(
            boolean specified,
            String value
    ) {
    }
}