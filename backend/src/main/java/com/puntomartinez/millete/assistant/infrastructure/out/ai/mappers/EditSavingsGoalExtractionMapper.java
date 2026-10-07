package com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers;

import com.puntomartinez.millete.assistant.domain.model.interpretation.EditSavingsGoalData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.FieldChange;
import com.puntomartinez.millete.assistant.domain.model.interpretation.SavingsGoalPriority;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditSavingsGoalExtractionDTO;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class EditSavingsGoalExtractionMapper {

    private EditSavingsGoalExtractionMapper() {
    }

    public static EditSavingsGoalData toDomain(
            EditSavingsGoalExtractionDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Extraction DTO cannot be null"
            );
        }

        var target =
                new EditSavingsGoalData.SavingsGoalTarget(
                        null,
                        normalizeNullable(dto.targetName())
                );

        var changes =
                new EditSavingsGoalData.SavingsGoalChanges(
                        toStringChange(dto.name()),
                        toAmountChange(dto.targetAmount()),
                        toPriorityChange(dto.priority()),
                        toDeadlineChange(dto.deadline()),
                        toLinkChange(dto.link())
                );

        return new EditSavingsGoalData(
                target,
                changes
        );
    }

    private static FieldChange<String> toStringChange(
            String value
    ) {
        if (value == null) {
            return FieldChange.unchanged();
        }

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "A specified name change cannot be blank"
            );
        }

        return FieldChange.set(
                value.trim()
        );
    }

    private static FieldChange<java.math.BigDecimal> toAmountChange(
            java.math.BigDecimal value
    ) {
        if (value == null) {
            return FieldChange.unchanged();
        }

        if (value.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "targetAmount must be greater than zero"
            );
        }

        return FieldChange.set(value);
    }

    private static FieldChange<SavingsGoalPriority> toPriorityChange(
            String value
    ) {
        if (value == null) {
            return FieldChange.unchanged();
        }

        String normalized =
                value.trim().toUpperCase();

        return switch (normalized) {
            case "LOW", "BAJA" ->
                    FieldChange.set(SavingsGoalPriority.LOW);

            case "MEDIUM", "MEDIA", "MEDIO", "NORMAL" ->
                    FieldChange.set(SavingsGoalPriority.MEDIUM);

            case "HIGH", "ALTA", "ALTO", "URGENTE" ->
                    FieldChange.set(SavingsGoalPriority.HIGH);

            default ->
                    throw new IllegalArgumentException(
                            "Invalid savings goal priority: " + value
                    );
        };
    }

    private static FieldChange<LocalDate> toDeadlineChange(
            String value
    ) {
        if (value == null) {
            return FieldChange.unchanged();
        }

        try {
            LocalDate deadline =
                    LocalDate.parse(value.trim());

            if (!deadline.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "deadline must be after today"
                );
            }

            return FieldChange.set(deadline);

        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid deadline: " + value,
                    exception
            );
        }
    }

    private static FieldChange<String> toLinkChange(
            String value
    ) {
        if (value == null) {
            return FieldChange.unchanged();
        }

        /*
         * Blank means "clear the link".
         * SavingsGoal.updateDetails() already interprets blank as null.
         */
        return FieldChange.set(value.trim());
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