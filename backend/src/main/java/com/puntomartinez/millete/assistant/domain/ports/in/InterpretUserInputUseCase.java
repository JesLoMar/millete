package com.millete.assistant.domain.ports.in;

import com.millete.assistant.domain.model.InterpretationResult;

public interface InterpretUserInputUseCase {

    InterpretationResult interpret(InterpretUserInputCommand command);
}