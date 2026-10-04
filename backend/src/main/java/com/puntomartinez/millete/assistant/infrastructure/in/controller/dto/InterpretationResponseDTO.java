package com.millete.assistant.infrastructure.in.controller.dto;

import com.millete.assistant.domain.model.AppAction;
import com.millete.assistant.domain.model.InterpretationResult;
import com.millete.assistant.domain.model.InterpretationStatus;

public record InterpretationResponseDTO(
        InterpretationStatus status,
        AppAction action,
        double probability,
        double margin,
        boolean abstain
) {

    public static InterpretationResponseDTO from(
            InterpretationResult result
    ) {
        return new InterpretationResponseDTO(
                result.status(),
                result.action(),
                result.confidence().probability(),
                result.confidence().margin(),
                result.confidence().abstain()
        );
    }
}