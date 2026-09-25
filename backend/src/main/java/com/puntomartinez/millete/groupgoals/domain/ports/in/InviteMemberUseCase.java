package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;

import java.time.Instant;
import java.util.UUID;

public interface InviteMemberUseCase {

    InvitationResult inviteMember(
            UUID goalId,
            UUID inviterUserId,
            InviteMemberCommand command
    );

    record InviteMemberCommand(String identifier) {
    }

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