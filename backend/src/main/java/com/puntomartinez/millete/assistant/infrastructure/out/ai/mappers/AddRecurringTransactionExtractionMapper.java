package com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers;

import com.puntomartinez.millete.assistant.domain.model.interpretation.AddRecurringTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.FrequencyType;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddRecurringTransactionExtractionDTO;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

public final class AddRecurringTransactionExtractionMapper {

    private AddRecurringTransactionExtractionMapper() {
    }

    public static AddRecurringTransactionData toDomain(
            AddRecurringTransactionExtractionDTO dto
    ) {
        Objects.requireNonNull(
                dto,
                "dto cannot be null"
        );

        var description =
                normalizeNullable(
                        dto.description()
                );

        var categoryName =
                normalizeNullable(
                        dto.categoryName()
                );

        if (dto.amount() != null
                && dto.amount().signum() <= 0) {

            throw new IllegalArgumentException(
                    "Recurring transaction amount must be greater than zero"
            );
        }

        var frequencyType =
                parseFrequencyType(
                        dto.frequencyType()
                );

        var frequencyInterval =
                dto.frequencyInterval();

        if (frequencyInterval != null
                && frequencyInterval <= 0) {

            throw new IllegalArgumentException(
                    "Recurring transaction frequency interval must be greater than zero"
            );
        }

        var startDate =
                parseDate(
                        dto.startDate(),
                        "startDate"
                );

        var endDate =
                parseDate(
                        dto.endDate(),
                        "endDate"
                );

        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "Recurring transaction endDate cannot be before startDate"
            );
        }

        return new AddRecurringTransactionData(
                description,
                dto.amount(),
                categoryName,
                frequencyType,
                frequencyInterval,
                startDate,
                endDate
        );
    }

    private static FrequencyType parseFrequencyType(
            String value
    ) {
        var normalized =
                normalizeNullable(
                        value
                );

        if (normalized == null) {
            return null;
        }

        try {
            return FrequencyType.valueOf(
                    normalized.toUpperCase(
                            Locale.ROOT
                    )
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid recurring transaction frequency type: "
                            + value,
                    exception
            );
        }
    }

    private static LocalDate parseDate(
            String value,
            String field
    ) {
        var normalized =
                normalizeNullable(
                        value
                );

        if (normalized == null) {
            return null;
        }

        try {
            return LocalDate.parse(
                    normalized
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid " + field + " date: " + value,
                    exception
            );
        }
    }

    private static String normalizeNullable(
            String value
    ) {
        if (value == null) {
            return null;
        }

        var normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}