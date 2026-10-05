package com.puntomartinez.millete.assistant.domain.ports.out;

import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;

public interface AiDataExtractor {

    InterpretationData extract(AiExtractionContext context);
}