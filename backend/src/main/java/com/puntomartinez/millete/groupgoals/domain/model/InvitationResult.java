package com.puntomartinez.millete.groupgoals.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationResult(
        UUID id,
        UUID goalId,
        String goalName,
        UUID inviterUserId,
        String inviterName,
        UUID invitedUserId,
        InvitationStatus status,
        LocalDateTime createdAt
) {
}