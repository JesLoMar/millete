package com.puntomartinez.millete.groupgoals.domain.model;

import java.time.Instant;
import java.util.UUID;

public record InvitationResult(
        UUID id,
        UUID goalId,
        String goalName,
        UUID inviterUserId,
        String inviterName,
        UUID invitedUserId,
        InvitationStatus status,
        Instant createdAt
) {
}