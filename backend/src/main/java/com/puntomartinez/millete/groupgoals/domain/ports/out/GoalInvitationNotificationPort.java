package com.puntomartinez.millete.groupgoals.domain.ports.out;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;

import java.util.UUID;

public interface GoalInvitationNotificationPort {

void createInvitationNotification(
        GoalInvitation invitation,
        String goalName,
        String inviterName
);

void markInvitationNotificationAsActioned(
        UUID userId,
        UUID invitationId
);
}