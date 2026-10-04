package com.millete.assistant.domain.ports.out;

import com.millete.assistant.domain.model.InterpretationResult;

public interface AiDecisionProvider {

    InterpretationResult decide(String input);
}