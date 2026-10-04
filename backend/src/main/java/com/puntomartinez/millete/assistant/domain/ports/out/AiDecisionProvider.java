package com.puntomartinez.millete.assistant.domain.ports.out;

import com.puntomartinez.millete.assistant.domain.model.InterpretationResult;

public interface AiDecisionProvider {

    InterpretationResult decide(String input);
}