package com.puntomartinez.millete.assistant.application.services;

import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.Confidence;
import com.puntomartinez.millete.assistant.domain.model.InterpretationResult;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddCategoryData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddRecurringTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AppliedDefault;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolutionStatus;
import com.puntomartinez.millete.assistant.domain.model.interpretation.EditTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.FieldChange;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.MissingField;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionResolutionStatus;
import com.puntomartinez.millete.assistant.domain.model.interpretation.UnresolvedEntity;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDecisionProvider;
import com.puntomartinez.millete.assistant.domain.ports.out.CategoryResolver;
import com.puntomartinez.millete.assistant.domain.ports.out.TransactionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AssistantService
        implements InterpretUserInputUseCase {

    private final AiDecisionProvider aiDecisionProvider;
    private final AiDataExtractor aiDataExtractor;
    private final CategoryResolver categoryResolver;
    private final TransactionResolver transactionResolver;

    public AssistantService(
            AiDecisionProvider aiDecisionProvider,
            @Qualifier("aiDataExtractorRouter")
            AiDataExtractor aiDataExtractor,
            CategoryResolver categoryResolver,
            TransactionResolver transactionResolver
    ) {
        this.aiDecisionProvider =
                Objects.requireNonNull(
                        aiDecisionProvider,
                        "aiDecisionProvider cannot be null"
                );

        this.aiDataExtractor =
                Objects.requireNonNull(
                        aiDataExtractor,
                        "aiDataExtractor cannot be null"
                );

        this.categoryResolver =
                Objects.requireNonNull(
                        categoryResolver,
                        "categoryResolver cannot be null"
                );

        this.transactionResolver =
                Objects.requireNonNull(
                        transactionResolver,
                        "transactionResolver cannot be null"
                );
    }

    @Override
    public InterpretationResult interpret(
            InterpretUserInputCommand command
    ) {
        Objects.requireNonNull(
                command,
                "command cannot be null"
        );

        var aiInterpretation =
                aiDecisionProvider.decide(
                        command.input()
                );

        if (aiInterpretation.action() == null) {
            return InterpretationResult.unknown(
                    aiInterpretation.confidence()
            );
        }

        var action =
                aiInterpretation.action();

        if (action == AppAction.ADD_CATEGORY) {
            return interpretAddCategory(
                    command,
                    aiInterpretation.confidence()
            );
        }

        if (action == AppAction.ADD_EXPENSE_TRANSACTION
                || action == AppAction.ADD_INCOME_TRANSACTION) {

            return interpretAddTransaction(
                    command,
                    aiInterpretation.confidence(),
                    action
            );
        }

        if (action == AppAction.EDIT_TRANSACTION) {
            return interpretEditTransaction(
                    command,
                    aiInterpretation.confidence()
            );
        }

        if (action == AppAction.ADD_RECURRING_EXPENSE_TRANSACTION
                || action == AppAction.ADD_RECURRING_INCOME_TRANSACTION) {

            return interpretAddRecurringTransaction(
                    command,
                    aiInterpretation.confidence(),
                    action
            );
        }

        return InterpretationResult.needsInformation(
                action,
                aiInterpretation.confidence()
        );
    }

    private InterpretationResult interpretAddCategory(
            InterpretUserInputCommand command,
            Confidence confidence
    ) {
        var data =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                AppAction.ADD_CATEGORY
                        )
                );

        var missingFields =
                findMissingFields(
                        AppAction.ADD_CATEGORY,
                        data
                );

        var defaultsApplied =
                List.of(
                        new AppliedDefault(
                                "color",
                                "The frontend applies the default category color"
                        )
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    AppAction.ADD_CATEGORY,
                    confidence,
                    data,
                    missingFields,
                    List.of(),
                    defaultsApplied
            );
        }

        return InterpretationResult.ready(
                AppAction.ADD_CATEGORY,
                confidence,
                data,
                defaultsApplied
        );
    }

    private InterpretationResult interpretAddTransaction(
            InterpretUserInputCommand command,
            Confidence confidence,
            AppAction action
    ) {
        var extractedData =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                action
                        )
                );

        var missingFields =
                findMissingFields(
                        action,
                        extractedData
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    action,
                    confidence,
                    extractedData,
                    missingFields,
                    List.of(),
                    List.of()
            );
        }

        if (!(extractedData instanceof AddTransactionData transactionData)) {
            throw new IllegalStateException(
                    "AI extractor returned invalid data for action: "
                            + action
            );
        }

        var categoryResolution =
                categoryResolver.resolve(
                        command.userId(),
                        transactionData.categoryName()
                );

        if (categoryResolution.status()
                != CategoryResolutionStatus.FOUND) {

            return InterpretationResult.needsInformation(
                    action,
                    confidence,
                    transactionData,
                    List.of(),
                    List.of(
                            toUnresolvedEntity(
                                    categoryResolution
                            )
                    ),
                    List.of()
            );
        }

        var resolvedCategory =
                categoryResolution.category();

        var resolvedData =
                new AddTransactionData(
                        transactionData.description(),
                        transactionData.amount(),
                        resolvedCategory.name(),
                        resolvedCategory.id()
                );

        return InterpretationResult.ready(
                action,
                confidence,
                resolvedData,
                List.of()
        );
    }

    private InterpretationResult interpretEditTransaction(
            InterpretUserInputCommand command,
            Confidence confidence
    ) {
        var extractedData =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                AppAction.EDIT_TRANSACTION
                        )
                );

        var missingFields =
                findMissingFields(
                        AppAction.EDIT_TRANSACTION,
                        extractedData
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    AppAction.EDIT_TRANSACTION,
                    confidence,
                    extractedData,
                    missingFields,
                    List.of(),
                    List.of()
            );
        }

        if (!(extractedData instanceof EditTransactionData editData)) {
            throw new IllegalStateException(
                    "AI extractor returned invalid data for EDIT_TRANSACTION"
            );
        }

        var transactionResolution =
                transactionResolver.resolve(
                        command.userId(),
                        editData.target()
                );

        if (transactionResolution.status()
                != TransactionResolutionStatus.FOUND) {

            return InterpretationResult.needsInformation(
                    AppAction.EDIT_TRANSACTION,
                    confidence,
                    editData,
                    List.of(),
                    List.of(
                            toUnresolvedEntity(
                                    transactionResolution
                            )
                    ),
                    List.of()
            );
        }

        var resolvedTransaction =
                transactionResolution.transaction();

        var resolvedChanges =
                resolveCategoryChange(
                        command.userId(),
                        editData.changes()
                );

        if (resolvedChanges.unresolvedEntity() != null) {
            return InterpretationResult.needsInformation(
                    AppAction.EDIT_TRANSACTION,
                    confidence,
                    editData,
                    List.of(),
                    List.of(
                            resolvedChanges.unresolvedEntity()
                    ),
                    List.of()
            );
        }

        var resolvedData =
                new EditTransactionData(
                        new EditTransactionData.TransactionTarget(
                                resolvedTransaction.id(),
                                resolvedTransaction.description(),
                                resolvedTransaction.amount(),
                                resolvedTransaction.categoryName(),
                                resolvedTransaction.date(),
                                resolvedTransaction.type()
                        ),
                        resolvedChanges.changes()
                );

        return InterpretationResult.ready(
                AppAction.EDIT_TRANSACTION,
                confidence,
                resolvedData,
                List.of()
        );
    }

    private InterpretationResult interpretAddRecurringTransaction(
            InterpretUserInputCommand command,
            Confidence confidence,
            AppAction action
    ) {
        var extractedData =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                action
                        )
                );

        if (!(extractedData instanceof AddRecurringTransactionData recurringData)) {
            throw new IllegalStateException(
                    "AI extractor returned invalid data for action: "
                            + action
            );
        }

        var defaultsApplied =
                new ArrayList<AppliedDefault>();

        var normalizedData =
                applyRecurringDefaults(
                        recurringData,
                        defaultsApplied
                );

        var missingFields =
                findMissingFields(
                        action,
                        normalizedData
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    action,
                    confidence,
                    normalizedData,
                    missingFields,
                    List.of(),
                    defaultsApplied
            );
        }

        var categoryResolution =
                categoryResolver.resolve(
                        command.userId(),
                        normalizedData.categoryName()
                );

        if (categoryResolution.status()
                != CategoryResolutionStatus.FOUND) {

            return InterpretationResult.needsInformation(
                    action,
                    confidence,
                    normalizedData,
                    List.of(),
                    List.of(
                            toUnresolvedEntity(
                                    categoryResolution
                            )
                    ),
                    defaultsApplied
            );
        }

        var resolvedCategory =
                categoryResolution.category();

        var resolvedData =
                new AddRecurringTransactionData(
                        normalizedData.description(),
                        normalizedData.amount(),
                        resolvedCategory.name(),
                        normalizedData.frequencyType(),
                        normalizedData.frequencyInterval(),
                        normalizedData.startDate(),
                        normalizedData.endDate()
                );

        return InterpretationResult.ready(
                action,
                confidence,
                resolvedData,
                defaultsApplied
        );
    }

    private AddRecurringTransactionData applyRecurringDefaults(
            AddRecurringTransactionData data,
            List<AppliedDefault> defaultsApplied
    ) {
        var frequencyInterval =
                data.frequencyInterval();

        if (frequencyInterval == null
                && data.frequencyType() != null) {

            frequencyInterval = 1;

            defaultsApplied.add(
                    new AppliedDefault(
                            "frequencyInterval",
                            "The interval defaults to 1 when a frequency unit is explicitly provided"
                    )
            );
        }

        var startDate =
                data.startDate();

        if (startDate == null) {
            startDate = LocalDate.now();

            defaultsApplied.add(
                    new AppliedDefault(
                            "startDate",
                            "The start date defaults to today when the user does not specify one"
                    )
            );
        }

        return new AddRecurringTransactionData(
                data.description(),
                data.amount(),
                data.categoryName(),
                data.frequencyType(),
                frequencyInterval,
                startDate,
                data.endDate()
        );
    }

    private ResolvedCategoryChange resolveCategoryChange(
            java.util.UUID userId,
            EditTransactionData.TransactionChanges changes
    ) {
        var categoryChange =
                changes.categoryName();

        if (!categoryChange.specified()) {
            return new ResolvedCategoryChange(
                    changes,
                    null
            );
        }

        if (categoryChange.value() == null) {
            return new ResolvedCategoryChange(
                    changes,
                    null
            );
        }

        var resolution =
                categoryResolver.resolve(
                        userId,
                        categoryChange.value()
                );

        if (resolution.status()
                != CategoryResolutionStatus.FOUND) {

            return new ResolvedCategoryChange(
                    changes,
                    toUnresolvedEntity(
                            resolution
                    )
            );
        }

        var resolvedCategory =
                resolution.category();

        var resolvedChanges =
                new EditTransactionData.TransactionChanges(
                        changes.description(),
                        changes.amount(),
                        FieldChange.set(
                                resolvedCategory.name()
                        ),
                        changes.type()
                );

        return new ResolvedCategoryChange(
                resolvedChanges,
                null
        );
    }

    private List<MissingField> findMissingFields(
            AppAction action,
            InterpretationData data
    ) {
        var missingFields =
                new ArrayList<MissingField>();

        if (action == AppAction.ADD_CATEGORY
                && data instanceof AddCategoryData categoryData) {

            if (categoryData.name() == null
                    || categoryData.name().isBlank()) {

                missingFields.add(
                        new MissingField("name")
                );
            }
        }

        if ((action == AppAction.ADD_EXPENSE_TRANSACTION
                || action == AppAction.ADD_INCOME_TRANSACTION)
                && data instanceof AddTransactionData transactionData) {

            if (transactionData.description() == null
                    || transactionData.description().isBlank()) {

                missingFields.add(
                        new MissingField("description")
                );
            }

            if (transactionData.amount() == null) {
                missingFields.add(
                        new MissingField("amount")
                );
            }

            if (transactionData.categoryName() == null
                    || transactionData.categoryName().isBlank()) {

                missingFields.add(
                        new MissingField("categoryName")
                );
            }
        }

        if (action == AppAction.EDIT_TRANSACTION
                && data instanceof EditTransactionData editData) {

            if (!editData.target().hasCriteria()) {
                missingFields.add(
                        new MissingField("target")
                );
            }

            if (!editData.changes().hasChanges()) {
                missingFields.add(
                        new MissingField("changes")
                );
            }
        }

        if ((action == AppAction.ADD_RECURRING_EXPENSE_TRANSACTION
                || action == AppAction.ADD_RECURRING_INCOME_TRANSACTION)
                && data instanceof AddRecurringTransactionData recurringData) {

            if (recurringData.description() == null
                    || recurringData.description().isBlank()) {

                missingFields.add(
                        new MissingField("description")
                );
            }

            if (recurringData.amount() == null) {
                missingFields.add(
                        new MissingField("amount")
                );
            }

            if (recurringData.categoryName() == null
                    || recurringData.categoryName().isBlank()) {

                missingFields.add(
                        new MissingField("categoryName")
                );
            }

            if (recurringData.frequencyType() == null) {
                missingFields.add(
                        new MissingField("frequencyType")
                );
            }

            if (recurringData.frequencyInterval() == null) {
                missingFields.add(
                        new MissingField("frequencyInterval")
                );
            }

            if (recurringData.startDate() == null) {
                missingFields.add(
                        new MissingField("startDate")
                );
            }

            if (recurringData.endDate() != null
                    && recurringData.startDate() != null
                    && recurringData.endDate()
                    .isBefore(recurringData.startDate())) {

                missingFields.add(
                        new MissingField("endDate")
                );
            }
        }

        return List.copyOf(
                missingFields
        );
    }

    private UnresolvedEntity toUnresolvedEntity(
            CategoryResolution resolution
    ) {
        var status =
                switch (resolution.status()) {
                    case NOT_FOUND ->
                            UnresolvedEntity.ResolutionStatus.NOT_FOUND;

                    case AMBIGUOUS ->
                            UnresolvedEntity.ResolutionStatus.AMBIGUOUS;

                    case FOUND ->
                            throw new IllegalStateException(
                                    "FOUND category cannot be unresolved"
                            );
                };

        return new UnresolvedEntity(
                UnresolvedEntity.EntityType.CATEGORY,
                resolution.reference(),
                status
        );
    }

    private UnresolvedEntity toUnresolvedEntity(
            TransactionResolution resolution
    ) {
        var status =
                switch (resolution.status()) {
                    case NOT_FOUND ->
                            UnresolvedEntity.ResolutionStatus.NOT_FOUND;

                    case AMBIGUOUS ->
                            UnresolvedEntity.ResolutionStatus.AMBIGUOUS;

                    case FOUND ->
                            throw new IllegalStateException(
                                    "FOUND transaction cannot be unresolved"
                            );
                };

        return new UnresolvedEntity(
                UnresolvedEntity.EntityType.TRANSACTION,
                resolution.reference(),
                status
        );
    }

    private record ResolvedCategoryChange(
            EditTransactionData.TransactionChanges changes,
            UnresolvedEntity unresolvedEntity
    ) {
    }
}