package com.millete.assistant.application.services;

import com.millete.assistant.domain.model.InterpretationResult;
import com.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.millete.assistant.domain.ports.out.AiDecisionProvider;
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
    public InterpretationResult interpret(InterpretUserInputCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        return aiDecisionProvider.decide(command.input());
    }
}