package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;
import com.puntomartinez.millete.groupgoals.domain.ports.in.AcceptInvitationUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.InviteMemberUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.ListPendingInvitationsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.RejectInvitationUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalInvitationRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.GetNotificationsUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsActionedUseCase;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupGoalInvitationService implements
        InviteMemberUseCase,
        AcceptInvitationUseCase,
        RejectInvitationUseCase,
        ListPendingInvitationsUseCase {

    private final GoalUnitRepository goalUnitRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalInvitationRepository goalInvitationRepository;
    private final UserRepository userRepository;

    private final CreateNotificationUseCase createNotificationUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationAsActionedUseCase markNotificationAsActionedUseCase;

    @Override
    @Transactional
    public InviteMemberUseCase.InvitationResult inviteMember(
            UUID goalId,
            UUID inviterUserId,
            InviteMemberCommand command) {

        GoalUnit goal = getGoal(goalId);

        GoalMember inviter = getMember(goalId, inviterUserId);

        if (!inviter.isAdmin()) {
            throw new ForbiddenOperationException(
                    "Solo el administrador puede invitar miembros."
            );
        }

        User inviterUser = userRepository
                .findById(inviterUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario invitador no existe."
                ));

        User invitedUser = userRepository
                .findByIdentifier(command.identifier())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe ningún usuario con ese identificador."
                ));

        GoalMember existingMember = goalMemberRepository
                .findByGoalIdAndUserId(
                        goalId,
                        invitedUser.getId()
                )
                .orElse(null);

        if (existingMember != null && existingMember.isActive()) {
            throw new ForbiddenOperationException(
                    "El usuario ya pertenece a este objetivo."
            );
        }

        GoalInvitation existingInvitation =
                goalInvitationRepository
                        .findByGoalIdAndInvitedUserIdAndStatus(
                                goalId,
                                invitedUser.getId(),
                                InvitationStatus.PENDING
                        )
                        .orElse(null);

        if (existingInvitation != null) {
            throw new ForbiddenOperationException(
                    "Ya existe una invitación pendiente para este usuario."
            );
        }

        GoalInvitation invitation = GoalInvitation.create(
                goalId,
                invitedUser.getEmail(),
                inviterUserId,
                invitedUser.getId()
        );

        GoalInvitation savedInvitation =
                goalInvitationRepository.save(invitation);

        createInvitationNotification(
                savedInvitation,
                goal,
                inviterUser,
                invitedUser
        );

        return toInviteResult(
                savedInvitation,
                goal,
                inviterUser
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListPendingInvitationsUseCase.InvitationResult> getPendingInvitations(
            UUID userId) {

        return goalInvitationRepository
                .findByInvitedUserIdAndStatus(
                        userId,
                        InvitationStatus.PENDING
                )
                .stream()
                .map(this::toPendingInvitationResult)
                .toList();
    }

    @Override
    @Transactional
    public GoalInvitation acceptInvitation(
            UUID userId,
            UUID invitationId) {

        GoalInvitation invitation = getInvitation(invitationId);

        validateInvitedUser(invitation, userId);

        if (!invitation.isAcceptable()) {
            throw new ForbiddenOperationException(
                    "La invitación ya no es válida."
            );
        }

        getGoal(invitation.getGoalId());

        GoalMember existingMember =
                goalMemberRepository
                        .findByGoalIdAndUserId(
                                invitation.getGoalId(),
                                userId
                        )
                        .orElse(null);

        if (existingMember == null) {

            GoalMember member = GoalMember.create(
                    invitation.getGoalId(),
                    userId,
                    GoalRole.MEMBER,
                    null
            );

            goalMemberRepository.save(member);

        } else {

            if (existingMember.isActive()) {
                throw new ForbiddenOperationException(
                        "El usuario ya pertenece a este objetivo."
                );
            }

            existingMember.activate();
            goalMemberRepository.save(existingMember);
        }

        invitation.markAsAccepted();

        GoalInvitation savedInvitation =
                goalInvitationRepository.save(invitation);

        markInvitationNotificationAsActioned(
                userId,
                invitationId
        );

        return savedInvitation;
    }

    @Override
    @Transactional
    public void rejectInvitation(
            UUID userId,
            UUID invitationId) {

        GoalInvitation invitation = getInvitation(invitationId);

        validateInvitedUser(invitation, userId);

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ForbiddenOperationException(
                    "La invitación ya no está pendiente."
            );
        }

        invitation.markAsRejected();

        goalInvitationRepository.save(invitation);

        markInvitationNotificationAsActioned(
                userId,
                invitationId
        );
    }

    private ListPendingInvitationsUseCase.InvitationResult toPendingInvitationResult(
            GoalInvitation invitation) {

        GoalUnit goal = goalUnitRepository
                .findById(invitation.getGoalId())
                .orElse(null);

        User inviter = invitation.getInviterUserId() == null
                ? null
                : userRepository
                .findById(invitation.getInviterUserId())
                .orElse(null);

        return toPendingInvitationResult(
                invitation,
                goal,
                inviter
        );
    }

    private ListPendingInvitationsUseCase.InvitationResult toPendingInvitationResult(
            GoalInvitation invitation,
            GoalUnit goal,
            User inviter) {

        String goalName = goal != null
                ? goal.getName()
                : null;

        String inviterName = inviter != null
                ? getUserDisplayName(inviter)
                : null;

        return new ListPendingInvitationsUseCase.InvitationResult(
                invitation.getId(),
                invitation.getGoalId(),
                goalName,
                invitation.getInviterUserId(),
                inviterName,
                invitation.getInvitedUserId(),
                invitation.getStatus().name(),
                invitation.getCreatedAt()
        );
    }

    private InviteMemberUseCase.InvitationResult toInviteResult(
            GoalInvitation invitation,
            GoalUnit goal,
            User inviter) {

        String goalName = goal != null
                ? goal.getName()
                : null;

        String inviterName = inviter != null
                ? getUserDisplayName(inviter)
                : null;

        return new InviteMemberUseCase.InvitationResult(
                invitation.getId(),
                invitation.getGoalId(),
                goalName,
                invitation.getInviterUserId(),
                inviterName,
                invitation.getInvitedUserId(),
                invitation.getStatus().name(),
                invitation.getCreatedAt()
        );
    }

    private void createInvitationNotification(
            GoalInvitation invitation,
            GoalUnit goal,
            User inviterUser,
            User invitedUser) {

        String inviterName = getUserDisplayName(inviterUser);

        createNotificationUseCase.create(
                new CreateNotificationUseCase.CreateNotificationCommand(
                        invitedUser.getId(),
                        NotificationType.GOAL_INVITATION,
                        "Invitación a objetivo",
                        inviterName
                                + " te ha invitado a participar en \""
                                + goal.getName()
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

    private void markInvitationNotificationAsActioned(
            UUID userId,
            UUID invitationId) {

        var notifications =
                getNotificationsUseCase.getUserNotifications(
                        userId,
                        100
                );

        notifications.stream()
                .filter(notification ->
                        notification.getType()
                                == NotificationType.GOAL_INVITATION)
                .filter(notification ->
                        notification.getMetadata() != null)
                .filter(notification -> {
                    Object metadataInvitationId =
                            notification.getMetadata()
                                    .get("invitationId");

                    return metadataInvitationId != null
                            && invitationId.toString()
                            .equals(metadataInvitationId.toString());
                })
                .findFirst()
                .ifPresent(notification ->
                        markNotificationAsActionedUseCase
                                .markAsActioned(
                                        userId,
                                        notification.getId()
                                )
                );
    }

    private GoalUnit getGoal(UUID goalId) {
        return goalUnitRepository
                .findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El objetivo no existe."
                ));
    }

    private GoalMember getMember(
            UUID goalId,
            UUID userId) {

        return goalMemberRepository
                .findByGoalIdAndUserId(goalId, userId)
                .filter(GoalMember::isActive)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "El usuario no pertenece al objetivo."
                ));
    }

    private GoalInvitation getInvitation(UUID invitationId) {
        return goalInvitationRepository
                .findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La invitación no existe."
                ));
    }

    private void validateInvitedUser(
            GoalInvitation invitation,
            UUID userId) {

        if (!userId.equals(invitation.getInvitedUserId())) {
            throw new ForbiddenOperationException(
                    "El usuario no puede gestionar esta invitación."
            );
        }
    }

    private String getUserDisplayName(User user) {
        return user.getUsername() != null
                ? user.getUsername()
                : user.getEmail();
    }
}