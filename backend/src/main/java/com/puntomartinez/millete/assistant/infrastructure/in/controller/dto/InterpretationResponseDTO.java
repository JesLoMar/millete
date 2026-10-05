package com.puntomartinez.millete.assistant.infrastructure.in.controller.dto;

import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.InterpretationResult;
import com.puntomartinez.millete.assistant.domain.model.InterpretationStatus;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AppliedDefault;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.MissingField;
import com.puntomartinez.millete.assistant.domain.model.interpretation.UnresolvedEntity;

import java.util.List;

public record InterpretationResponseDTO(
        InterpretationStatus status,
        AppAction action,
        double probability,
        double margin,
        boolean abstain,
        InterpretationData data,
        List<MissingField> missingFields,
        List<UnresolvedEntity> unresolvedEntities,
        List<AppliedDefault> defaultsApplied
) {

    public static InterpretationResponseDTO from(
            InterpretationResult result
    ) {
        return new InterpretationResponseDTO(
                result.status(),
                result.action(),
                result.confidence().probability(),
                result.confidence().margin(),
                result.confidence().abstain(),
                result.data(),
                result.missingFields(),
                result.unresolvedEntities(),
                result.defaultsApplied()
        );
    }
}