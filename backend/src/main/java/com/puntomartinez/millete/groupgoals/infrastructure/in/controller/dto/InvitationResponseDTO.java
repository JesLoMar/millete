package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationResponseDTO(
        UUID id,
        UUID goalId,
        String goalName,
        UUID inviterUserId,
        String inviterName,
        UUID invitedUserId,
        String status,
        LocalDateTime createdAt
) {
}