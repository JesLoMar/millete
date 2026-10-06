package com.puntomartinez.millete.assistant.infrastructure.out.ai.mappers;

import com.puntomartinez.millete.assistant.domain.model.interpretation.EditTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.FieldChange;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionType;
import com.puntomartinez.millete.assistant.infrastructure.out.ai.dto.EditTransactionExtractionDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

public final class EditTransactionExtractionMapper {

    private EditTransactionExtractionMapper() {
    }

    public static EditTransactionData toDomain(
            EditTransactionExtractionDTO dto
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

        var target =
                new EditTransactionData.TransactionTarget(
                        null,
                        normalizeNullable(
                                dto.target().description()
                        ),
                        dto.target().amount(),
                        normalizeNullable(
                                dto.target().categoryName()
                        ),
                        parseDate(
                                dto.target().date()
                        ),
                        parseTransactionType(
                                dto.target().type()
                        )
                );

        validateTargetAmount(
                target.amount()
        );

        var changes =
                dto.changes();

        var descriptionChange =
                toStringChange(
                        changes.description()
                );

        var amountChange =
                toAmountChange(
                        changes.amount()
                );

        var categoryChange =
                toStringChange(
                        changes.categoryName()
                );

        var typeChange =
                toTypeChange(
                        changes.type()
                );

        return new EditTransactionData(
                target,
                new EditTransactionData.TransactionChanges(
                        descriptionChange,
                        amountChange,
                        categoryChange,
                        typeChange
                )
        );
    }

    private static FieldChange<String> toStringChange(
            EditTransactionExtractionDTO.StringChangeDTO change
    ) {
        Objects.requireNonNull(
                change,
                "string change cannot be null"
        );

        var value =
                normalizeNullable(
                        change.value()
                );

        if (!change.specified()) {
            return FieldChange.unchanged();
        }

        if (value == null) {
            return FieldChange.clear();
        }

        return FieldChange.set(
                value
        );
    }

    private static FieldChange<BigDecimal> toAmountChange(
            EditTransactionExtractionDTO.AmountChangeDTO change
    ) {
        Objects.requireNonNull(
                change,
                "amount change cannot be null"
        );

        if (!change.specified()) {
            return FieldChange.unchanged();
        }

        if (change.value() == null) {
            throw new IllegalArgumentException(
                    "Transaction amount cannot be cleared"
            );
        }

        validateAmount(
                change.value()
        );

        return FieldChange.set(
                change.value()
        );
    }

    private static FieldChange<TransactionType> toTypeChange(
            EditTransactionExtractionDTO.TypeChangeDTO change
    ) {
        Objects.requireNonNull(
                change,
                "type change cannot be null"
        );

        if (!change.specified()) {
            return FieldChange.unchanged();
        }

        if (change.value() == null
                || change.value().isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction type cannot be cleared"
            );
        }

        return FieldChange.set(
                parseRequiredTransactionType(
                        change.value()
                )
        );
    }

    private static TransactionType parseTransactionType(
            String value
    ) {
        if (value == null
                || value.isBlank()) {

            return null;
        }

        return parseRequiredTransactionType(
                value
        );
    }

    private static TransactionType parseRequiredTransactionType(
            String value
    ) {
        try {
            return TransactionType.valueOf(
                    value.trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            );

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid transaction type: "
                            + value,
                    exception
            );
        }
    }

    private static LocalDate parseDate(
            String value
    ) {
        if (value == null
                || value.isBlank()) {

            return null;
        }

        try {
            return LocalDate.parse(
                    value.trim()
            );

        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid transaction target date: "
                            + value,
                    exception
            );
        }
    }

    private static void validateTargetAmount(
            BigDecimal amount
    ) {
        if (amount != null) {
            validateAmount(
                    amount
            );
        }
    }

    private static void validateAmount(
            BigDecimal amount
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero"
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