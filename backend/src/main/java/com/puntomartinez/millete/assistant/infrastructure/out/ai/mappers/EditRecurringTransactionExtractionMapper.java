package com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers;

import com.puntomartinez.millete.assistant.domain.model.interpretation.EditRecurringTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.FieldChange;
import com.puntomartinez.millete.assistant.domain.model.interpretation.FrequencyType;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionType;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditRecurringTransactionExtractionDTO;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public final class EditRecurringTransactionExtractionMapper {

    private EditRecurringTransactionExtractionMapper() {
    }

    public static EditRecurringTransactionData toDomain(
            EditRecurringTransactionExtractionDTO dto
    ) {
        Objects.requireNonNull(
                dto,
                "dto cannot be null"
        );

        Objects.requireNonNull(
                dto.target(),
                "target cannot be null"
        );

        Objects.requireNonNull(
                dto.changes(),
                "changes cannot be null"
        );

        validateTarget(
                dto.target()
        );

        return new EditRecurringTransactionData(
                new EditRecurringTransactionData.RecurringTransactionTarget(
                        normalizeNullable(
                                dto.target().description()
                        ),
                        validateTargetAmount(
                                dto.target().amount()
                        ),
                        normalizeNullable(
                                dto.target().categoryName()
                        ),
                        parseFrequencyType(
                                dto.target().frequencyType()
                        ),
                        validateTargetInterval(
                                dto.target().frequencyInterval()
                        ),
                        parseTransactionType(
                                dto.target().type()
                        )
                ),
                new EditRecurringTransactionData.RecurringTransactionChanges(
                        parseRequiredChange(
                                dto.changes().description(),
                                "description"
                        ),
                        parseRequiredAmountChange(
                                dto.changes().amount()
                        ),
                        parseRequiredTransactionTypeChange(
                                dto.changes().type()
                        ),
                        parseRequiredFrequencyTypeChange(
                                dto.changes().frequencyType()
                        ),
                        parseRequiredIntegerChange(
                                dto.changes().frequencyInterval(),
                                "frequencyInterval"
                        )
                )
        );
    }

    private static void validateTarget(
            EditRecurringTransactionExtractionDTO.TargetDTO target
    ) {
        if (target.description() != null
                && target.description().isBlank()) {
            throw new IllegalArgumentException(
                    "Recurring transaction target description cannot be blank"
            );
        }
    }

    private static BigDecimal validateTargetAmount(
            BigDecimal amount
    ) {
        if (amount != null
                && amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Recurring transaction target amount must be greater than zero"
            );
        }

        return amount;
    }

    private static Integer validateTargetInterval(
            Integer interval
    ) {
        if (interval != null
                && interval <= 0) {
            throw new IllegalArgumentException(
                    "Recurring transaction target frequency interval must be greater than zero"
            );
        }

        return interval;
    }

    private static FieldChange<String> parseRequiredChange(
            EditRecurringTransactionExtractionDTO.FieldChangeDTO<String> dto,
            String field
    ) {
        Objects.requireNonNull(
                dto,
                field + " change cannot be null"
        );

        if (!Boolean.TRUE.equals(dto.specified())) {
            return FieldChange.unchanged();
        }

        var value =
                normalizeNullable(
                        dto.value()
                );

        if (value == null) {
            throw new IllegalArgumentException(
                    field + " cannot be cleared"
            );
        }

        return FieldChange.set(
                value
        );
    }

    private static FieldChange<BigDecimal> parseRequiredAmountChange(
            EditRecurringTransactionExtractionDTO.FieldChangeDTO<BigDecimal> dto
    ) {
        Objects.requireNonNull(
                dto,
                "amount change cannot be null"
        );

        if (!Boolean.TRUE.equals(dto.specified())) {
            return FieldChange.unchanged();
        }

        if (dto.value() == null
                || dto.value().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount change must be greater than zero"
            );
        }

        return FieldChange.set(
                dto.value()
        );
    }

    private static FieldChange<TransactionType> parseRequiredTransactionTypeChange(
            EditRecurringTransactionExtractionDTO.FieldChangeDTO<String> dto
    ) {
        Objects.requireNonNull(
                dto,
                "type change cannot be null"
        );

        if (!Boolean.TRUE.equals(dto.specified())) {
            return FieldChange.unchanged();
        }

        var value =
                parseTransactionType(
                        dto.value()
                );

        if (value == null) {
            throw new IllegalArgumentException(
                    "Specified transaction type change cannot be null"
            );
        }

        return FieldChange.set(
                value
        );
    }

    private static FieldChange<FrequencyType> parseRequiredFrequencyTypeChange(
            EditRecurringTransactionExtractionDTO.FieldChangeDTO<String> dto
    ) {
        Objects.requireNonNull(
                dto,
                "frequencyType change cannot be null"
        );

        if (!Boolean.TRUE.equals(dto.specified())) {
            return FieldChange.unchanged();
        }

        var value =
                parseFrequencyType(
                        dto.value()
                );

        if (value == null) {
            throw new IllegalArgumentException(
                    "Specified frequency type change cannot be null"
            );
        }

        return FieldChange.set(
                value
        );
    }

    private static FieldChange<Integer> parseRequiredIntegerChange(
            EditRecurringTransactionExtractionDTO.FieldChangeDTO<Integer> dto,
            String field
    ) {
        Objects.requireNonNull(
                dto,
                field + " change cannot be null"
        );

        if (!Boolean.TRUE.equals(dto.specified())) {
            return FieldChange.unchanged();
        }

        if (dto.value() == null
                || dto.value() <= 0) {
            throw new IllegalArgumentException(
                    field + " change must be greater than zero"
            );
        }

        return FieldChange.set(
                dto.value()
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
                    "Invalid recurring frequency type: " + value,
                    exception
            );
        }
    }

    private static TransactionType parseTransactionType(
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
            return TransactionType.valueOf(
                    normalized.toUpperCase(
                            Locale.ROOT
                    )
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid transaction type: " + value,
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