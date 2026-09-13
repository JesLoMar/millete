package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ListPendingInvitationsUseCase {

    List<InvitationResult> getPendingInvitations(UUID userId);

    record InvitationResult(
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
}