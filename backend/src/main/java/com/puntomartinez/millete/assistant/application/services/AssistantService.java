package com.puntomartinez.millete.assistant.application.services;

import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.InterpretationResult;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddCategoryData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AppliedDefault;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolution;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolutionStatus;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.MissingField;
import com.puntomartinez.millete.assistant.domain.model.interpretation.UnresolvedEntity;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDecisionProvider;
import com.puntomartinez.millete.assistant.domain.ports.out.CategoryResolver;
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

    public AssistantService(
            AiDecisionProvider aiDecisionProvider,
            @Qualifier("aiDataExtractorRouter")
            AiDataExtractor aiDataExtractor,
            CategoryResolver categoryResolver
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

        /*
         * ADD_CATEGORY is already fully implemented.
         */
        if (action == AppAction.ADD_CATEGORY) {
            return interpretAddCategory(
                    command,
                    aiInterpretation.confidence()
            );
        }

        /*
         * ADD_EXPENSE_TRANSACTION is now connected.
         */
        if (action
                == AppAction.ADD_EXPENSE_TRANSACTION) {

            return interpretAddExpenseTransaction(
                    command,
                    aiInterpretation.confidence()
            );
        }

        /*
         * Other actions are still not connected.
         */
        return InterpretationResult.needsInformation(
                action,
                aiInterpretation.confidence()
        );
    }

    private InterpretationResult interpretAddCategory(
            InterpretUserInputCommand command,
            com.puntomartinez.millete.assistant.domain.model.Confidence confidence
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

    private InterpretationResult interpretAddExpenseTransaction(
            InterpretUserInputCommand command,
            com.puntomartinez.millete.assistant.domain.model.Confidence confidence
    ) {
        var extractedData =
                aiDataExtractor.extract(
                        new AiExtractionContext(
                                command.input(),
                                AppAction.ADD_EXPENSE_TRANSACTION
                        )
                );

        var missingFields =
                findMissingFields(
                        AppAction.ADD_EXPENSE_TRANSACTION,
                        extractedData
                );

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    AppAction.ADD_EXPENSE_TRANSACTION,
                    confidence,
                    extractedData,
                    missingFields,
                    List.of(),
                    List.of()
            );
        }

        var transactionData =
                (AddTransactionData) extractedData;

        var categoryResolution =
                categoryResolver.resolve(
                        command.userId(),
                        transactionData.categoryName()
                );

        if (categoryResolution.status()
                != CategoryResolutionStatus.FOUND) {

            return InterpretationResult.needsInformation(
                    AppAction.ADD_EXPENSE_TRANSACTION,
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

        var resolvedData =
                new AddTransactionData(
                        transactionData.description(),
                        transactionData.amount(),
                        transactionData.categoryName(),
                        categoryResolution.category().id()
                );

        return InterpretationResult.ready(
                AppAction.ADD_EXPENSE_TRANSACTION,
                confidence,
                resolvedData,
                List.of()
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

        if (action
                == AppAction.ADD_EXPENSE_TRANSACTION
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
}