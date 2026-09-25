package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponseDTO(
        UUID id,
        UUID goalId,
        String goalName,
        UUID inviterUserId,
        String inviterName,
        UUID invitedUserId,
        String status,
        Instant createdAt
) {
}