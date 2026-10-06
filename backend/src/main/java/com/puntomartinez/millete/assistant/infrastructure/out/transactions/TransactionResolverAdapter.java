package com.puntomartinez.millete.assistant.infrastructure.out.transactions;

import com.puntomartinez.millete.assistant.domain.model.interpretation.EditTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionCandidate;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionType;
import com.puntomartinez.millete.assistant.domain.ports.out.TransactionResolver;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.adapters.VerdictTransactionSelector;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public final class TransactionResolverAdapter
        implements TransactionResolver {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final VerdictTransactionSelector verdictTransactionSelector;

    public TransactionResolverAdapter(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            VerdictTransactionSelector verdictTransactionSelector
    ) {
        this.transactionRepository =
                Objects.requireNonNull(
                        transactionRepository,
                        "transactionRepository cannot be null"
                );

        this.categoryRepository =
                Objects.requireNonNull(
                        categoryRepository,
                        "categoryRepository cannot be null"
                );

        this.verdictTransactionSelector =
                Objects.requireNonNull(
                        verdictTransactionSelector,
                        "verdictTransactionSelector cannot be null"
                );
    }

    @Override
    public TransactionResolution resolve(
            UUID userId,
            EditTransactionData.TransactionTarget target
    ) {
        Objects.requireNonNull(
                userId,
                "userId cannot be null"
        );

        Objects.requireNonNull(
                target,
                "target cannot be null"
        );

        if (!target.hasCriteria()) {
            throw new IllegalArgumentException(
                    "Transaction target must contain at least one criterion"
            );
        }

        var categoryNames =
                categoryRepository.findByUserId(userId)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Category::getId,
                                        Category::getName,
                                        (first, second) -> first
                                )
                        );

        var candidates =
                transactionRepository.findAllByUserId(userId)
                        .stream()
                        .filter(this::isEditableTransaction)
                        .map(transaction ->
                                toCandidate(
                                        transaction,
                                        categoryNames
                                )
                        )
                        .toList();

        if (candidates.isEmpty()) {
            return TransactionResolution.notFound(
                    buildReference(target),
                    0.0,
                    0.0
            );
        }

        var narrowedCandidates =
                candidates.stream()
                        .filter(candidate ->
                                matchesExactTarget(
                                        target,
                                        candidate
                                )
                        )
                        .toList();

        if (narrowedCandidates.size() == 1) {
            return TransactionResolution.found(
                    buildReference(target),
                    narrowedCandidates.getFirst(),
                    1.0,
                    1.0
            );
        }

        if (!narrowedCandidates.isEmpty()) {
            return verdictTransactionSelector.select(
                    target,
                    narrowedCandidates
            );
        }

        return verdictTransactionSelector.select(
                target,
                candidates
        );
    }

    private boolean isEditableTransaction(
            Transaction transaction
    ) {
        return transaction.getType()
                == Transaction.TransactionType.INCOME
                || transaction.getType()
                == Transaction.TransactionType.EXPENSE;
    }

    private TransactionCandidate toCandidate(
            Transaction transaction,
            Map<UUID, String> categoryNames
    ) {
        return new TransactionCandidate(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategoryId() == null
                        ? null
                        : categoryNames.get(
                                transaction.getCategoryId()
                        ),
                transaction.getDate(),
                toAssistantTransactionType(
                        transaction.getType()
                )
        );
    }

    private TransactionType toAssistantTransactionType(
            Transaction.TransactionType type
    ) {
        return switch (type) {
            case INCOME ->
                    TransactionType.INCOME;

            case EXPENSE ->
                    TransactionType.EXPENSE;

            case TRANSFER_IN,
                 TRANSFER_OUT ->
                    throw new IllegalArgumentException(
                            "Investment transfer cannot be resolved as editable transaction"
                    );
        };
    }

    private boolean matchesExactTarget(
            EditTransactionData.TransactionTarget target,
            TransactionCandidate candidate
    ) {
        if (target.description() != null
                && !normalize(
                        target.description()
                ).equals(
                        normalize(
                                candidate.description()
                        )
                )) {

            return false;
        }

        if (target.amount() != null
                && target.amount().compareTo(
                        candidate.amount()
                ) != 0) {

            return false;
        }

        if (target.categoryName() != null
                && !normalize(
                        target.categoryName()
                ).equals(
                        normalizeNullable(
                                candidate.categoryName()
                        )
                )) {

            return false;
        }

        if (target.date() != null
                && !target.date().equals(
                        candidate.date()
                )) {

            return false;
        }

        if (target.type() != null
                && target.type() != candidate.type()) {

            return false;
        }

        return true;
    }

    private String buildReference(
            EditTransactionData.TransactionTarget target
    ) {
        var builder =
                new StringBuilder();

        if (target.description() != null) {
            append(
                    builder,
                    "description",
                    target.description()
            );
        }

        if (target.amount() != null) {
            append(
                    builder,
                    "amount",
                    target.amount().toString()
            );
        }

        if (target.categoryName() != null) {
            append(
                    builder,
                    "category",
                    target.categoryName()
            );
        }

        if (target.date() != null) {
            append(
                    builder,
                    "date",
                    target.date().toString()
            );
        }

        if (target.type() != null) {
            append(
                    builder,
                    "type",
                    target.type().name()
            );
        }

        return builder.length() == 0
                ? "transaction"
                : builder.toString();
    }

    private void append(
            StringBuilder builder,
            String key,
            String value
    ) {
        if (builder.length() > 0) {
            builder.append(", ");
        }

        builder
                .append(key)
                .append("=")
                .append(value);
    }

    private static String normalizeNullable(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return normalize(
                value
        );
    }

    private static String normalize(
            String value
    ) {
        var normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFD
                );

        return normalized
                .replaceAll(
                        "\\p{M}",
                        ""
                )
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}