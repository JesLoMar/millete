package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ListPendingInvitationsUseCase {

    List<InvitationResult> listPendingInvitations(UUID userId);

    record InvitationResult(
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
}