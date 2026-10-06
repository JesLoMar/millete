package com.puntomartinez.millete.assistant.infrastructure.out.plannedtransactions;

import com.puntomartinez.millete.assistant.domain.model.interpretation.EditRecurringTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.FrequencyType;
import com.puntomartinez.millete.assistant.domain.model.interpretation.RecurringTransactionResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.RecurringTransactionResolution.RecurringTransactionCandidate;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionType;
import com.puntomartinez.millete.assistant.domain.ports.out.RecurringTransactionResolver;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Component
public final class RecurringTransactionResolverAdapter
        implements RecurringTransactionResolver {

    private final PlannedTransactionRepository plannedTransactionRepository;
    private final CategoryRepository categoryRepository;

    public RecurringTransactionResolverAdapter(
            PlannedTransactionRepository plannedTransactionRepository,
            CategoryRepository categoryRepository
    ) {
        this.plannedTransactionRepository =
                Objects.requireNonNull(
                        plannedTransactionRepository,
                        "plannedTransactionRepository cannot be null"
                );

        this.categoryRepository =
                Objects.requireNonNull(
                        categoryRepository,
                        "categoryRepository cannot be null"
                );
    }

    @Override
    public RecurringTransactionResolution resolve(
            UUID userId,
            EditRecurringTransactionData.RecurringTransactionTarget target,
            UUID categoryId
    ) {
        Objects.requireNonNull(
                userId,
                "userId cannot be null"
        );

        Objects.requireNonNull(
                target,
                "target cannot be null"
        );

        var reference =
                buildReference(
                        target
                );

        List<PlannedTransaction> candidates =
                plannedTransactionRepository
                        .findAllByUserId(userId)
                        .stream()
                        .filter(PlannedTransaction::isActive)
                        .filter(transaction ->
                                matches(
                                        transaction,
                                        target,
                                        categoryId
                                )
                        )
                        .toList();

        if (candidates.isEmpty()) {
            return RecurringTransactionResolution.notFound(
                    reference
            );
        }

        if (candidates.size() > 1) {
            return RecurringTransactionResolution.ambiguous(
                    reference
            );
        }

        PlannedTransaction transaction =
                candidates.getFirst();

        String categoryName =
                resolveCategoryName(
                        transaction.getCategoryId(),
                        userId
                );

        var candidate =
                new RecurringTransactionCandidate(
                        transaction.getId(),
                        transaction.getDescription(),
                        transaction.getAmount(),
                        categoryName,
                        toAssistantFrequencyType(
                                transaction.getFrequencyType()
                        ),
                        transaction.getFrequencyInterval(),
                        toAssistantTransactionType(
                                transaction.getType()
                        )
                );

        return RecurringTransactionResolution.found(
                reference,
                candidate
        );
    }

    private boolean matches(
            PlannedTransaction transaction,
            EditRecurringTransactionData.RecurringTransactionTarget target,
            UUID categoryId
    ) {
        if (target.description() != null
                && !normalize(
                        transaction.getDescription()
                ).equals(
                        normalize(
                                target.description()
                        )
                )) {
            return false;
        }

        if (target.amount() != null
                && transaction.getAmount().compareTo(
                        target.amount()
                ) != 0) {
            return false;
        }

        if (categoryId != null
                && !Objects.equals(
                        transaction.getCategoryId(),
                        categoryId
                )) {
            return false;
        }

        if (target.frequencyType() != null
                && toAssistantFrequencyType(
                        transaction.getFrequencyType()
                ) != target.frequencyType()) {
            return false;
        }

        if (target.frequencyInterval() != null
                && !Objects.equals(
                        transaction.getFrequencyInterval(),
                        target.frequencyInterval()
                )) {
            return false;
        }

        if (target.type() != null
                && toAssistantTransactionType(
                        transaction.getType()
                ) != target.type()) {
            return false;
        }

        return true;
    }

    private String resolveCategoryName(
            UUID categoryId,
            UUID userId
    ) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository
                .findByIdAndUserId(
                        categoryId,
                        userId
                )
                .map(Category::getName)
                .orElse(null);
    }

    private FrequencyType toAssistantFrequencyType(
            PlannedTransaction.FrequencyType frequencyType
    ) {
        return FrequencyType.valueOf(
                frequencyType.name()
        );
    }

    private TransactionType toAssistantTransactionType(
            com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType type
    ) {
        return TransactionType.valueOf(
                type.name()
        );
    }

    private String buildReference(
            EditRecurringTransactionData.RecurringTransactionTarget target
    ) {
        if (target.description() != null
                && !target.description().isBlank()) {
            return target.description().trim();
        }

        if (target.categoryName() != null
                && !target.categoryName().isBlank()) {
            return target.categoryName().trim();
        }

        if (target.amount() != null) {
            return target.amount().toPlainString();
        }

        return "recurring transaction";
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