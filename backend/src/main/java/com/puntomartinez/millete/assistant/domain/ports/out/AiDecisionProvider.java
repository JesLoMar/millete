package com.puntomartinez.millete.assistant.domain.ports.out;

import com.puntomartinez.millete.assistant.domain.model.interpretation.AiInterpretation;

public interface AiDecisionProvider {

    AiInterpretation decide(String input);
}