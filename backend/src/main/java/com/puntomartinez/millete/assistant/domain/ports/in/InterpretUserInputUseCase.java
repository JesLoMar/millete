package com.puntomartinez.millete.assistant.domain.ports.in;

import com.puntomartinez.millete.assistant.domain.model.InterpretationResult;

public interface InterpretUserInputUseCase {

    InterpretationResult interpret(InterpretUserInputCommand command);
}