package com.puntomartinez.millete.assistant.domain.model;

import java.util.Objects;

public record InterpretationResult(
        InterpretationStatus status,
        AppAction action,
        Confidence confidence
) {

    public InterpretationResult(
            InterpretationStatus status,
            AppAction action,
            Confidence confidence
    ) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.action = action;
        this.confidence = Objects.requireNonNull(
                confidence,
                "confidence cannot be null"
        );

        if (status == InterpretationStatus.READY && action == null) {
            throw new IllegalArgumentException(
                    "Action is required when interpretation status is READY"
            );
        }

        if (status == InterpretationStatus.UNKNOWN && action != null) {
            throw new IllegalArgumentException(
                    "Action must be null when interpretation status is UNKNOWN"
            );
        }
    }

    public static InterpretationResult ready(
            AppAction action,
            Confidence confidence
    ) {
        return new InterpretationResult(
                InterpretationStatus.READY,
                Objects.requireNonNull(action, "action cannot be null"),
                confidence
        );
    }

    public static InterpretationResult needsInformation(
            AppAction action,
            Confidence confidence
    ) {
        return new InterpretationResult(
                InterpretationStatus.NEEDS_INFORMATION,
                Objects.requireNonNull(action, "action cannot be null"),
                confidence
        );
    }

    public static InterpretationResult unknown(
            Confidence confidence
    ) {
        return new InterpretationResult(
                InterpretationStatus.UNKNOWN,
                null,
                confidence
        );
    }
}