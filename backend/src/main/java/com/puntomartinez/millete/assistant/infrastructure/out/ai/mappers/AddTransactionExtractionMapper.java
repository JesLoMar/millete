package com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers;

import com.puntomartinez.millete.assistant.domain.model.interpretation.AddTransactionData;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddTransactionExtractionDTO;

import java.util.Objects;

public final class AddTransactionExtractionMapper {

    private AddTransactionExtractionMapper() {
    }

    public static AddTransactionData toDomain(
            AddTransactionExtractionDTO dto
    ) {
        Objects.requireNonNull(
                dto,
                "dto cannot be null"
        );

        var description =
                normalizeNullable(dto.description());

        var categoryName =
                normalizeNullable(dto.categoryName());

        if (dto.amount() != null
                && dto.amount().signum() <= 0) {

            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero"
            );
        }

        return new AddTransactionData(
                description,
                dto.amount(),
                categoryName,
                null
        );
    }

    private static String normalizeNullable(
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