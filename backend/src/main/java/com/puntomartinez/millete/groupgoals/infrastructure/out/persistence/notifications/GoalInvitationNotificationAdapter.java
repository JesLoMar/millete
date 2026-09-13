package com.puntomartinez.millete.groupgoals.infrastructure.out.notifications;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalInvitationNotificationPort;
import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.GetNotificationsUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsActionedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoalInvitationNotificationAdapter
implements GoalInvitationNotificationPort {

private final CreateNotificationUseCase createNotificationUseCase;
private final GetNotificationsUseCase getNotificationsUseCase;
private final MarkNotificationAsActionedUseCase markNotificationAsActionedUseCase;

@Override
public void createInvitationNotification(
        GoalInvitation invitation,
        String goalName,
        String inviterName) {

    createNotificationUseCase.create(
            new CreateNotificationUseCase.CreateNotificationCommand(
                    invitation.getInvitedUserId(),
                    NotificationType.GOAL_INVITATION,
                    "Invitación a objetivo",
                    inviterName
                            + " te ha invitado a participar en \""
                            + goalName
                            + "\".",
                    Map.of(
                            "invitationId",
                            invitation.getId().toString(),
                            "goalId",
                            invitation.getGoalId().toString()
                    ),
                    true,
                    invitation.getExpiresAt()
            )
    );
}

@Override
public void markInvitationNotificationAsActioned(
        UUID userId,
        UUID invitationId) {

    List<Notification> notifications =
            getNotificationsUseCase
                    .findUserNotificationsByTypeAndMetadataValue(
                            userId,
                            NotificationType.GOAL_INVITATION.name(),
                            "invitationId",
                            invitationId.toString()
                    );

    notifications.stream()
            .findFirst()
            .ifPresent(notification ->
                    markNotificationAsActionedUseCase
                            .markAsActioned(
                                    userId,
                                    notification.getId()
                            )
            );
}
}