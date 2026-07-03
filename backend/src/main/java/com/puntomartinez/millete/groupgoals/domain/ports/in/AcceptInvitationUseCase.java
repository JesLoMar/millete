package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import java.util.UUID;

public interface AcceptInvitationUseCase {
    GoalInvitation acceptInvitation(UUID userId, UUID invitationId);
}
