package com.puntomartinez.millete.investments.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ActivityAudit(
        UUID id,
        UUID activityId,
        UUID userId,
        String beforeJson,
        String afterJson,
        String reason,
        Instant changedAt
) {

    public ActivityAudit {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del audit es obligatorio"
            );
        }

        if (activityId == null) {
            throw new IllegalArgumentException(
                    "El identificador de la Activity es obligatorio"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "El identificador del usuario es obligatorio"
            );
        }

        if (beforeJson == null || afterJson == null) {
            throw new IllegalArgumentException(
                    "Los snapshots del audit son obligatorios"
            );
        }

        if (changedAt == null) {
            throw new IllegalArgumentException(
                    "La fecha del cambio es obligatoria"
            );
        }

        if (reason != null && reason.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar los 500 caracteres"
            );
        }

        reason = reason == null
                ? null
                : reason.trim();
    }
}