package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.util.UUID;

public interface RejectInvitationUseCase {

    void rejectInvitation(
            UUID userId,
            UUID invitationId
    );
}