package com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers;

import com.puntomartinez.millete.assistant.domain.model.interpretation.AddSavingsGoalContributionData;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddSavingsGoalContributionExtractionDTO;

import java.math.BigDecimal;

public final class AddSavingsGoalContributionExtractionMapper {

    private AddSavingsGoalContributionExtractionMapper() {
    }

    public static AddSavingsGoalContributionData toDomain(
            AddSavingsGoalContributionExtractionDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Extraction DTO cannot be null"
            );
        }

        BigDecimal amount = dto.amount();

        if (amount != null
                && amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Contribution amount must be greater than zero"
            );
        }

        var target =
                new AddSavingsGoalContributionData.SavingsGoalTarget(
                        null,
                        normalizeNullable(dto.targetName())
                );

        return new AddSavingsGoalContributionData(
                target,
                amount
        );
    }

    private static String normalizeNullable(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}