package com.puntomartinez.millete.assistant.application.services;

import com.puntomartinez.millete.assistant.domain.model.InterpretationResult;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDecisionProvider;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AssistantService implements InterpretUserInputUseCase {

    private final AiDecisionProvider aiDecisionProvider;

    public AssistantService(AiDecisionProvider aiDecisionProvider) {
        this.aiDecisionProvider = Objects.requireNonNull(
                aiDecisionProvider,
                "aiDecisionProvider cannot be null"
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
            aiDecisionProvider.decide(command.input());

    if (aiInterpretation.action() == null) {
        return InterpretationResult.unknown(
                aiInterpretation.confidence()
        );
    }

    return InterpretationResult.needsInformation(
            aiInterpretation.action(),
            aiInterpretation.confidence()
    );
}
}