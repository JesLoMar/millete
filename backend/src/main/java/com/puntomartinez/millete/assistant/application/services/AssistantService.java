package com.puntomartinez.millete.assistant.application.services;

import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.InterpretationResult;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AddCategoryData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.MissingField;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDecisionProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AssistantService implements InterpretUserInputUseCase {

    private final AiDecisionProvider aiDecisionProvider;
    private final AiDataExtractor aiDataExtractor;

    public AssistantService(
            AiDecisionProvider aiDecisionProvider,
            AiDataExtractor aiDataExtractor
    ) {
        this.aiDecisionProvider = Objects.requireNonNull(
                aiDecisionProvider,
                "aiDecisionProvider cannot be null"
        );

        this.aiDataExtractor = Objects.requireNonNull(
                aiDataExtractor,
                "aiDataExtractor cannot be null"
        );
    }

    @Override
    public InterpretationResult interpret(
            InterpretUserInputCommand command
    ) {
        Objects.requireNonNull(command, "command cannot be null");

        var aiInterpretation =
                aiDecisionProvider.decide(command.input());

        if (aiInterpretation.action() == null) {
            return InterpretationResult.unknown(
                    aiInterpretation.confidence()
            );
        }

        var action = aiInterpretation.action();

        /*
         * Transitional implementation:
         * only ADD_CATEGORY has data extraction connected so far.
         */
        if (action != AppAction.ADD_CATEGORY) {
            return InterpretationResult.needsInformation(
                    action,
                    aiInterpretation.confidence()
            );
        }

        var extractionContext = new AiExtractionContext(
                command.input(),
                action
        );

        var data = aiDataExtractor.extract(extractionContext);

        var missingFields = findMissingFields(action, data);

        if (!missingFields.isEmpty()) {
            return InterpretationResult.needsInformation(
                    action,
                    aiInterpretation.confidence(),
                    data,
                    missingFields,
                    List.of(),
                    List.of()
            );
        }

        return InterpretationResult.ready(
                action,
                aiInterpretation.confidence(),
                data,
                List.of()
        );
    }

    private List<MissingField> findMissingFields(
            AppAction action,
            Object data
    ) {
        if (action == AppAction.ADD_CATEGORY
                && data instanceof AddCategoryData categoryData) {

            var missingFields = new java.util.ArrayList<MissingField>();

            if (categoryData.name() == null
                    || categoryData.name().isBlank()) {
                missingFields.add(
                        new MissingField("name")
                );
            }

            return List.copyOf(missingFields);
        }

        return List.of();
    }
}