package com.puntomartinez.millete.investments.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ActivityAudit(UUID id, UUID activityId, UUID userId,
                            String beforeJson, String afterJson,
                            String reason, Instant changedAt) {
    public ActivityAudit {
        if (id == null || activityId == null || userId == null || changedAt == null) throw new IllegalArgumentException("Audit identifiers and time are required");
        if (beforeJson == null || afterJson == null) throw new IllegalArgumentException("Audit snapshots are required");
        if (reason != null && reason.length() > 500) throw new IllegalArgumentException("Audit reason exceeds 500 characters");
    }
}
