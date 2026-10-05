package com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers;

import com.puntomartinez.millete.assistant.domain.model.interpretation.AddCategoryData;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddCategoryExtractionDTO;

import java.util.Objects;

public final class AddCategoryExtractionMapper {

    private AddCategoryExtractionMapper() {
    }

    public static AddCategoryData toDomain(
            AddCategoryExtractionDTO dto
    ) {
        Objects.requireNonNull(
                dto,
                "dto cannot be null"
        );

        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException(
                    "Extracted category name cannot be blank"
            );
        }

        if (dto.budgetLimit() != null
                && dto.budgetLimit().signum() < 0) {
            throw new IllegalArgumentException(
                    "Extracted category budget limit cannot be negative"
            );
        }

        return new AddCategoryData(
                dto.name().trim(),
                normalizeNullableText(dto.description()),
                dto.budgetLimit()
        );
    }

    private static String normalizeNullableText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        var normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}