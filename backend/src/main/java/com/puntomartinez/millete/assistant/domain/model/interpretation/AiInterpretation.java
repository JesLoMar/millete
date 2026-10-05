package com.puntomartinez.millete.assistant.domain.model.interpretation;

import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.Confidence;

import java.util.Objects;

public record AiInterpretation(
        AppAction action,
        Confidence confidence,
        InterpretationData data
) {

    public AiInterpretation {
        Objects.requireNonNull(
                confidence,
                "confidence cannot be null"
        );

        if (action == null && data != null) {
            throw new IllegalArgumentException(
                    "Data cannot exist when action is null"
            );
        }
    }

    public static AiInterpretation of(
            AppAction action,
            Confidence confidence,
            InterpretationData data
    ) {
        return new AiInterpretation(
                Objects.requireNonNull(
                        action,
                        "action cannot be null"
                ),
                confidence,
                data
        );
    }

    public static AiInterpretation unknown(
            Confidence confidence
    ) {
        return new AiInterpretation(
                null,
                confidence,
                null
        );
    }
}