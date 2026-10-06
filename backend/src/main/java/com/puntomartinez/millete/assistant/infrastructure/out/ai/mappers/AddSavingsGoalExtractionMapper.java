package com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers;

import com.puntomartinez.millete.assistant.domain.model.interpretation.AddSavingsGoalData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.SavingsGoalPriority;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.AddSavingsGoalExtractionDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

public final class AddSavingsGoalExtractionMapper {

    private AddSavingsGoalExtractionMapper() {
    }

    public static AddSavingsGoalData toDomain(
            AddSavingsGoalExtractionDTO dto
    ) {
        Objects.requireNonNull(
                dto,
                "dto cannot be null"
        );

        var name =
                normalizeNullable(dto.name());

        var priority =
                parsePriority(dto.priority());

        var deadline =
                parseDeadline(dto.deadline());

        var link =
                normalizeNullable(dto.link());

        if (dto.targetAmount() != null
                && dto.targetAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Savings goal target amount must be greater than zero"
            );
        }

        if (deadline != null
                && !deadline.isAfter(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Savings goal deadline must be after today"
            );
        }

        return new AddSavingsGoalData(
                name,
                dto.targetAmount(),
                priority,
                deadline,
                link
        );
    }

    private static SavingsGoalPriority parsePriority(
            String value
    ) {
        var normalized =
                normalizeNullable(value);

        if (normalized == null) {
            return null;
        }

        try {
            return SavingsGoalPriority.valueOf(
                    normalized.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid savings goal priority: " + value,
                    exception
            );
        }
    }

    private static LocalDate parseDeadline(
            String value
    ) {
        var normalized =
                normalizeNullable(value);

        if (normalized == null) {
            return null;
        }

        try {
            return LocalDate.parse(
                    normalized
            );
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid savings goal deadline: " + value,
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