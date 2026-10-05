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
        Objects.requireNonNull(dto, "dto cannot be null");

        var name = normalizeNullable(dto.name());
        var description = normalizeNullable(dto.description());

        if (dto.budgetLimit() != null
                && dto.budgetLimit().signum() < 0) {
            throw new IllegalArgumentException(
                    "Budget limit cannot be negative"
            );
        }

        return new AddCategoryData(
                name,
                description,
                dto.budgetLimit()
        );
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        var normalized = value.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}