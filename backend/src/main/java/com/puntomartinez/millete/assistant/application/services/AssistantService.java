package com.puntomartinez.millete.assistant.application.services;

import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.Confidence;
import com.puntomartinez.millete.assistant.domain.model.InterpretationResult;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddCategoryData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddRecurringTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddSavingsGoalContributionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddSavingsGoalData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AppliedDefault;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolutionStatus;
import com.puntomartinez.millete.assistant.domain.model.interpretation.EditRecurringTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.EditSavingsGoalData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.EditTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.FieldChange;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.MissingField;
import com.puntomartinez.millete.assistant.domain.model.interpretation.RecurringTransactionResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.RecurringTransactionResolution.RecurringTransactionResolutionStatus;
import com.puntomartinez.millete.assistant.domain.model.interpretation.SavingsGoalPriority;
import com.puntomartinez.millete.assistant.domain.model.interpretation.SavingsGoalResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.SavingsGoalResolutionStatus;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionResolutionStatus;
import com.puntomartinez.millete.assistant.domain.model.interpretation.UnresolvedEntity;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDecisionProvider;
import com.puntomartinez.millete.assistant.domain.ports.out.CategoryResolver;
import com.puntomartinez.millete.assistant.domain.ports.out.RecurringTransactionResolver;
import com.puntomartinez.millete.assistant.domain.ports.out.SavingsGoalResolver;
import com.puntomartinez.millete.assistant.domain.ports.out.TransactionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
    private final RecurringTransactionResolver recurringTransactionResolver;
    private final SavingsGoalResolver savingsGoalResolver;

    public AssistantService(
            AiDecisionProvider aiDecisionProvider,
            @Qualifier("aiDataExtractorRouter")
            AiDataExtractor aiDataExtractor,
            CategoryResolver categoryResolver,
            TransactionResolver transactionResolver,
            RecurringTransactionResolver recurringTransactionResolver,
            SavingsGoalResolver savingsGoalResolver
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

        this.recurringTransactionResolver =
                Objects.requireNonNull(
                        recurringTransactionResolver,
                        "recurringTransactionResolver cannot be null"
                );

        this.savingsGoalResolver =
                Objects.requireNonNull(
                        savingsGoalResolver,
                        "savingsGoalResolver cannot be null"
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

        if (action == AppAction.EDIT_RECURRING_TRANSACTION) {
            return interpretEditRecurringTransaction(
                    command,
                    aiInterpretation.confidence()
            );
        }

        if (action == AppAction.ADD_SAVING_GOAL) {
            return interpretAddSavingsGoal(
                    command,
                    aiInterpretation.confidence()
            );
        }

        if (action == AppAction.EDIT_SAVING_GOAL) {
            return interpretEditSavingsGoal(
                    command,
                    aiInterpretation.confidence()
            );
        }

        if (action == AppAction.ADD_SAVING_GOAL_CONTRIBUTION) {
            return interpretAddSavingsGoalContribution(
                    command,
                    aiInterpretation.confidence()
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

    private InterpretationResult interpretEditRecurringTransaction(
            InterpretUserInputCommand command,
            Confidence confidence
    ) {
        var extractedData =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                AppAction.EDIT_RECURRING_TRANSACTION
                        )
                );

        if (!(extractedData instanceof EditRecurringTransactionData editData)) {
            throw new IllegalStateException(
                    "AI extractor returned invalid data for EDIT_RECURRING_TRANSACTION"
            );
        }

        var defaultsApplied =
                new ArrayList<AppliedDefault>();

        var normalizedData =
                applyRecurringEditDefaults(
                        editData,
                        defaultsApplied
                );

        var missingFields =
                findMissingFields(
                        AppAction.EDIT_RECURRING_TRANSACTION,
                        normalizedData
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    AppAction.EDIT_RECURRING_TRANSACTION,
                    confidence,
                    normalizedData,
                    missingFields,
                    List.of(),
                    defaultsApplied
            );
        }

        java.util.UUID categoryId = null;

        if (normalizedData.target().categoryName() != null) {

            var categoryResolution =
                    categoryResolver.resolve(
                            command.userId(),
                            normalizedData.target().categoryName()
                    );

            if (categoryResolution.status()
                    != CategoryResolutionStatus.FOUND) {

                return InterpretationResult.needsInformation(
                        AppAction.EDIT_RECURRING_TRANSACTION,
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

            categoryId =
                    categoryResolution.category().id();
        }

        var recurringResolution =
                recurringTransactionResolver.resolve(
                        command.userId(),
                        normalizedData.target(),
                        categoryId
                );

        if (recurringResolution.status()
                != RecurringTransactionResolutionStatus.FOUND) {

            return InterpretationResult.needsInformation(
                    AppAction.EDIT_RECURRING_TRANSACTION,
                    confidence,
                    normalizedData,
                    List.of(),
                    List.of(
                            toUnresolvedEntity(
                                    recurringResolution
                            )
                    ),
                    defaultsApplied
            );
        }

        var resolvedTransaction =
                recurringResolution.transaction();

        var resolvedTarget =
                new EditRecurringTransactionData.RecurringTransactionTarget(
                        resolvedTransaction.description(),
                        resolvedTransaction.amount(),
                        resolvedTransaction.categoryName(),
                        resolvedTransaction.frequencyType(),
                        resolvedTransaction.frequencyInterval(),
                        resolvedTransaction.type()
                );

        var resolvedData =
                new EditRecurringTransactionData(
                        resolvedTarget,
                        normalizedData.changes()
                );

        return InterpretationResult.ready(
                AppAction.EDIT_RECURRING_TRANSACTION,
                confidence,
                resolvedData,
                defaultsApplied
        );
    }

    private InterpretationResult interpretAddSavingsGoal(
            InterpretUserInputCommand command,
            Confidence confidence
    ) {
        var extractedData =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                AppAction.ADD_SAVING_GOAL
                        )
                );

        if (!(extractedData instanceof AddSavingsGoalData savingsGoalData)) {
            throw new IllegalStateException(
                    "AI extractor returned invalid data for ADD_SAVING_GOAL"
            );
        }

        var defaultsApplied =
                new ArrayList<AppliedDefault>();

        var normalizedData =
                applySavingsGoalDefaults(
                        savingsGoalData,
                        defaultsApplied
                );

        var missingFields =
                findMissingFields(
                        AppAction.ADD_SAVING_GOAL,
                        normalizedData
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    AppAction.ADD_SAVING_GOAL,
                    confidence,
                    normalizedData,
                    missingFields,
                    List.of(),
                    defaultsApplied
            );
        }

        return InterpretationResult.ready(
                AppAction.ADD_SAVING_GOAL,
                confidence,
                normalizedData,
                defaultsApplied
        );
    }

    private InterpretationResult interpretEditSavingsGoal(
            InterpretUserInputCommand command,
            Confidence confidence
    ) {
        var extractedData =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                AppAction.EDIT_SAVING_GOAL
                        )
                );

        if (!(extractedData instanceof EditSavingsGoalData editData)) {
            throw new IllegalStateException(
                    "AI extractor returned invalid data for EDIT_SAVING_GOAL"
            );
        }

        var missingFields =
                findMissingFields(
                        AppAction.EDIT_SAVING_GOAL,
                        editData
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    AppAction.EDIT_SAVING_GOAL,
                    confidence,
                    editData,
                    missingFields,
                    List.of(),
                    List.of()
            );
        }

        var resolution =
                savingsGoalResolver.resolve(
                        command.userId(),
                        editData.target().name()
                );

        if (resolution.status()
                != SavingsGoalResolutionStatus.FOUND) {

            return InterpretationResult.needsInformation(
                    AppAction.EDIT_SAVING_GOAL,
                    confidence,
                    editData,
                    List.of(),
                    List.of(
                            toUnresolvedEntity(
                                    resolution
                            )
                    ),
                    List.of()
            );
        }

        var goal =
                resolution.goal();

        var resolvedTarget =
                new EditSavingsGoalData.SavingsGoalTarget(
                        goal.id(),
                        goal.name()
                );

        var resolvedData =
                new EditSavingsGoalData(
                        resolvedTarget,
                        editData.changes()
                );

        return InterpretationResult.ready(
                AppAction.EDIT_SAVING_GOAL,
                confidence,
                resolvedData,
                List.of()
        );
    }

    private InterpretationResult interpretAddSavingsGoalContribution(
            InterpretUserInputCommand command,
            Confidence confidence
    ) {
        var extractedData =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                AppAction.ADD_SAVING_GOAL_CONTRIBUTION
                        )
                );

        if (!(extractedData instanceof AddSavingsGoalContributionData contributionData)) {
            throw new IllegalStateException(
                    "AI extractor returned invalid data for "
                            + "ADD_SAVING_GOAL_CONTRIBUTION"
            );
        }

        var missingFields =
                findMissingFields(
                        AppAction.ADD_SAVING_GOAL_CONTRIBUTION,
                        contributionData
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    AppAction.ADD_SAVING_GOAL_CONTRIBUTION,
                    confidence,
                    contributionData,
                    missingFields,
                    List.of(),
                    List.of()
            );
        }

        var resolution =
                savingsGoalResolver.resolve(
                        command.userId(),
                        contributionData.target().name()
                );

        if (resolution.status()
                != SavingsGoalResolutionStatus.FOUND) {

            return InterpretationResult.needsInformation(
                    AppAction.ADD_SAVING_GOAL_CONTRIBUTION,
                    confidence,
                    contributionData,
                    List.of(),
                    List.of(
                            toUnresolvedEntity(
                                    resolution
                            )
                    ),
                    List.of()
            );
        }

        var goal =
                resolution.goal();

        var resolvedTarget =
                new AddSavingsGoalContributionData.SavingsGoalTarget(
                        goal.id(),
                        goal.name()
                );

        var resolvedData =
                new AddSavingsGoalContributionData(
                        resolvedTarget,
                        contributionData.amount()
                );

        return InterpretationResult.ready(
                AppAction.ADD_SAVING_GOAL_CONTRIBUTION,
                confidence,
                resolvedData,
                List.of()
        );
    }

    private AddSavingsGoalData applySavingsGoalDefaults(
            AddSavingsGoalData data,
            List<AppliedDefault> defaultsApplied
    ) {
        var priority =
                data.priority();

        if (priority == null) {
            priority =
                    SavingsGoalPriority.MEDIUM;

            defaultsApplied.add(
                    new AppliedDefault(
                            "priority",
                            "The priority defaults to MEDIUM when the user does not specify one"
                    )
            );
        }

        return new AddSavingsGoalData(
                data.name(),
                data.targetAmount(),
                priority,
                data.deadline(),
                data.link()
        );
    }

    private EditRecurringTransactionData applyRecurringEditDefaults(
            EditRecurringTransactionData data,
            List<AppliedDefault> defaultsApplied
    ) {
        var changes =
                data.changes();

        if (changes.frequencyType().specified()
                && !changes.frequencyInterval().specified()) {

            changes =
                    new EditRecurringTransactionData.RecurringTransactionChanges(
                            changes.description(),
                            changes.amount(),
                            changes.type(),
                            changes.frequencyType(),
                            FieldChange.set(
                                    1
                            )
                    );

            defaultsApplied.add(
                    new AppliedDefault(
                            "frequencyInterval",
                            "The interval defaults to 1 when the frequency unit is changed without an explicit interval"
                    )
            );
        }

        return new EditRecurringTransactionData(
                data.target(),
                changes
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
            startDate = java.time.LocalDate.now();

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

        if (action == AppAction.EDIT_RECURRING_TRANSACTION
                && data instanceof EditRecurringTransactionData editData) {

            if (!editData.hasTargetCriteria()) {
                missingFields.add(
                        new MissingField("target")
                );
            }

            if (!editData.hasChanges()) {
                missingFields.add(
                        new MissingField("changes")
                );
            }
        }

        if (action == AppAction.ADD_SAVING_GOAL
                && data instanceof AddSavingsGoalData savingsGoalData) {

            if (savingsGoalData.name() == null
                    || savingsGoalData.name().isBlank()) {

                missingFields.add(
                        new MissingField("name")
                );
            }

            if (savingsGoalData.targetAmount() == null) {
                missingFields.add(
                        new MissingField("targetAmount")
                );
            }
        }

        if (action == AppAction.EDIT_SAVING_GOAL
                && data instanceof EditSavingsGoalData editData) {

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

        if (action == AppAction.ADD_SAVING_GOAL_CONTRIBUTION
                && data instanceof AddSavingsGoalContributionData contributionData) {

            if (contributionData.target().name() == null
                    || contributionData.target().name().isBlank()) {

                missingFields.add(
                        new MissingField("target")
                );
            }

            if (contributionData.amount() == null) {
                missingFields.add(
                        new MissingField("amount")
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

    private UnresolvedEntity toUnresolvedEntity(
            RecurringTransactionResolution resolution
    ) {
        var status =
                switch (resolution.status()) {
                    case NOT_FOUND ->
                            UnresolvedEntity.ResolutionStatus.NOT_FOUND;

                    case AMBIGUOUS ->
                            UnresolvedEntity.ResolutionStatus.AMBIGUOUS;

                    case FOUND ->
                            throw new IllegalStateException(
                                    "FOUND recurring transaction cannot be unresolved"
                            );
                };

        return new UnresolvedEntity(
                UnresolvedEntity.EntityType.RECURRING_TRANSACTION,
                resolution.reference(),
                status
        );
    }

    private UnresolvedEntity toUnresolvedEntity(
            SavingsGoalResolution resolution
    ) {
        var status =
                switch (resolution.status()) {
                    case NOT_FOUND ->
                            UnresolvedEntity.ResolutionStatus.NOT_FOUND;

                    case AMBIGUOUS ->
                            UnresolvedEntity.ResolutionStatus.AMBIGUOUS;

                    case FOUND ->
                            throw new IllegalStateException(
                                    "FOUND savings goal cannot be unresolved"
                            );
                };

        return new UnresolvedEntity(
                UnresolvedEntity.EntityType.SAVINGS_GOAL,
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