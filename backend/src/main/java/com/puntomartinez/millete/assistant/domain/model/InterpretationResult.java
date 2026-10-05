package com.puntomartinez.millete.assistant.domain.model;

import com.puntomartinez.millete.assistant.domain.model.interpretation.AppliedDefault;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.MissingField;
import com.puntomartinez.millete.assistant.domain.model.interpretation.UnresolvedEntity;

import java.util.List;
import java.util.Objects;

public record InterpretationResult(
        InterpretationStatus status,
        AppAction action,
        Confidence confidence,
        InterpretationData data,
        List<MissingField> missingFields,
        List<UnresolvedEntity> unresolvedEntities,
        List<AppliedDefault> defaultsApplied
) {

    public InterpretationResult {
        Objects.requireNonNull(
                status,
                "status cannot be null"
        );

        Objects.requireNonNull(
                confidence,
                "confidence cannot be null"
        );

        missingFields = List.copyOf(
                Objects.requireNonNull(
                        missingFields,
                        "missingFields cannot be null"
                )
        );

        unresolvedEntities = List.copyOf(
                Objects.requireNonNull(
                        unresolvedEntities,
                        "unresolvedEntities cannot be null"
                )
        );

        defaultsApplied = List.copyOf(
                Objects.requireNonNull(
                        defaultsApplied,
                        "defaultsApplied cannot be null"
                )
        );

        switch (status) {
            case READY -> {
                if (action == null) {
                    throw new IllegalArgumentException(
                            "Action is required when interpretation status is READY"
                    );
                }

                if (data == null) {
                    throw new IllegalArgumentException(
                            "Data is required when interpretation status is READY"
                    );
                }

                if (!missingFields.isEmpty()) {
                    throw new IllegalArgumentException(
                            "READY interpretation cannot have missing fields"
                    );
                }

                if (!unresolvedEntities.isEmpty()) {
                    throw new IllegalArgumentException(
                            "READY interpretation cannot have unresolved entities"
                    );
                }
            }

            case NEEDS_INFORMATION -> {
                if (action == null) {
                    throw new IllegalArgumentException(
                            "Action is required when interpretation status is NEEDS_INFORMATION"
                    );
                }
            }

            case UNKNOWN -> {
                if (action != null) {
                    throw new IllegalArgumentException(
                            "Action must be null when interpretation status is UNKNOWN"
                    );
                }

                if (data != null) {
                    throw new IllegalArgumentException(
                            "Data must be null when interpretation status is UNKNOWN"
                    );
                }

                if (!missingFields.isEmpty()) {
                    throw new IllegalArgumentException(
                            "UNKNOWN interpretation cannot have missing fields"
                    );
                }

                if (!unresolvedEntities.isEmpty()) {
                    throw new IllegalArgumentException(
                            "UNKNOWN interpretation cannot have unresolved entities"
                    );
                }
            }
        }
    }

    /*
 * Transitional factory used by the current classifier-only implementation.
 * It will disappear once the AI provider starts returning extracted data.
 */
public static InterpretationResult ready(
        AppAction action,
        Confidence confidence
) {
    return new InterpretationResult(
            InterpretationStatus.READY,
            Objects.requireNonNull(
                    action,
                    "action cannot be null"
            ),
            confidence,
            null,
            List.of(),
            List.of(),
            List.of()
    );
}

public static InterpretationResult ready(
        AppAction action,
        Confidence confidence,
        InterpretationData data,
        List<AppliedDefault> defaultsApplied
) {
    return new InterpretationResult(
            InterpretationStatus.READY,
            Objects.requireNonNull(
                    action,
                    "action cannot be null"
            ),
            confidence,
            Objects.requireNonNull(
                    data,
                    "data cannot be null"
            ),
            List.of(),
            List.of(),
            Objects.requireNonNull(
                    defaultsApplied,
                    "defaultsApplied cannot be null"
            )
    );
}

    public static InterpretationResult needsInformation(
            AppAction action,
            Confidence confidence,
            InterpretationData data,
            List<MissingField> missingFields,
            List<UnresolvedEntity> unresolvedEntities,
            List<AppliedDefault> defaultsApplied
    ) {
        return new InterpretationResult(
                InterpretationStatus.NEEDS_INFORMATION,
                Objects.requireNonNull(
                        action,
                        "action cannot be null"
                ),
                confidence,
                data,
                missingFields,
                unresolvedEntities,
                defaultsApplied
        );
    }

    public static InterpretationResult needsInformation(
            AppAction action,
            Confidence confidence
    ) {
        return needsInformation(
                action,
                confidence,
                null,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public static InterpretationResult unknown(
            Confidence confidence
    ) {
        return new InterpretationResult(
                InterpretationStatus.UNKNOWN,
                null,
                confidence,
                null,
                List.of(),
                List.of(),
                List.of()
        );
    }
}