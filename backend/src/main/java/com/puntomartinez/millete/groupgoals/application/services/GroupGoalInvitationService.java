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
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalInvitationNotificationPort;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalInvitationRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.UserLookupPort;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final UserLookupPort userLookupPort;
    private final GoalInvitationNotificationPort goalInvitationNotificationPort;

    @Override
    @Transactional
    public InviteMemberUseCase.InvitationResult inviteMember(
            UUID goalId,
            UUID inviterUserId,
            InviteMemberCommand command) {

        GoalUnit goal = getGoal(goalId);

        GoalMember inviter =
                getMember(goalId, inviterUserId);

        if (!inviter.isAdmin()) {
            throw new ForbiddenOperationException(
                    "Solo el administrador puede invitar miembros."
            );
        }

        UserLookupPort.UserInfo inviterUser =
                userLookupPort
                        .findById(inviterUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El usuario invitador no existe."
                                ));

        UserLookupPort.UserInfo invitedUser =
                userLookupPort
                        .findByIdentifier(command.identifier())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No existe ningún usuario con ese identificador."
                                ));

        GoalMember existingMember =
                goalMemberRepository
                        .findByGoalIdAndUserId(
                                goalId,
                                invitedUser.id()
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
                                invitedUser.id(),
                                InvitationStatus.PENDING
                        )
                        .orElse(null);

        if (existingInvitation != null) {
            throw new ForbiddenOperationException(
                    "Ya existe una invitación pendiente para este usuario."
            );
        }

        GoalInvitation invitation =
                GoalInvitation.create(
                        goalId,
                        invitedUser.email(),
                        inviterUserId,
                        invitedUser.id()
                );

        GoalInvitation savedInvitation =
                goalInvitationRepository.save(invitation);

        goalInvitationNotificationPort.createInvitationNotification(
                savedInvitation,
                goal.getName(),
                getUserDisplayName(inviterUser)
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

        List<GoalInvitation> invitations =
                goalInvitationRepository
                        .findByInvitedUserIdAndStatus(
                                userId,
                                InvitationStatus.PENDING
                        );

        Set<UUID> inviterIds = new HashSet<>();

        invitations.stream()
                .map(GoalInvitation::getInviterUserId)
                .filter(java.util.Objects::nonNull)
                .forEach(inviterIds::add);

        Map<UUID, UserLookupPort.UserInfo> usersById =
                userLookupPort.findByIds(inviterIds);

        Set<UUID> goalIds = invitations.stream()
                .map(GoalInvitation::getGoalId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, GoalUnit> goalsById =
                goalUnitRepository.findByIds(goalIds)
                        .stream()
                        .collect(Collectors.toMap(
                                GoalUnit::getId,
                                goal -> goal
                        ));

        return invitations.stream()
                .map(invitation ->
                        toPendingInvitationResult(
                                invitation,
                                goalsById,
                                usersById
                        ))
                .toList();
    }

    @Override
    @Transactional
    public GoalInvitation acceptInvitation(
            UUID userId,
            UUID invitationId) {

        GoalInvitation invitation =
                getInvitation(invitationId);

        validateInvitedUser(
                invitation,
                userId
        );

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

            GoalMember member =
                    GoalMember.create(
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

        goalInvitationNotificationPort
                .markInvitationNotificationAsActioned(
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

        GoalInvitation invitation =
                getInvitation(invitationId);

        validateInvitedUser(
                invitation,
                userId
        );

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ForbiddenOperationException(
                    "La invitación ya no está pendiente."
            );
        }

        invitation.markAsRejected();

        goalInvitationRepository.save(invitation);

        goalInvitationNotificationPort
                .markInvitationNotificationAsActioned(
                        userId,
                        invitationId
                );
    }

    private ListPendingInvitationsUseCase.InvitationResult toPendingInvitationResult(
            GoalInvitation invitation,
            Map<UUID, GoalUnit> goalsById,
            Map<UUID, UserLookupPort.UserInfo> usersById) {

        GoalUnit goal =
                goalsById.get(invitation.getGoalId());

        UserLookupPort.UserInfo inviter =
                invitation.getInviterUserId() == null
                        ? null
                        : usersById.get(
                                invitation.getInviterUserId()
                        );

        return toPendingInvitationResult(
                invitation,
                goal,
                inviter
        );
    }

    private ListPendingInvitationsUseCase.InvitationResult toPendingInvitationResult(
            GoalInvitation invitation,
            GoalUnit goal,
            UserLookupPort.UserInfo inviter) {

        String goalName =
                goal != null
                        ? goal.getName()
                        : null;

        String inviterName =
                inviter != null
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
            UserLookupPort.UserInfo inviter) {

        String goalName =
                goal != null
                        ? goal.getName()
                        : null;

        String inviterName =
                inviter != null
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

    private GoalUnit getGoal(UUID goalId) {
        return goalUnitRepository
                .findById(goalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "El objetivo no existe."
                        ));
    }

    private GoalMember getMember(
            UUID goalId,
            UUID userId) {

        return goalMemberRepository
                .findByGoalIdAndUserId(
                        goalId,
                        userId
                )
                .filter(GoalMember::isActive)
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "El usuario no pertenece al objetivo."
                        ));
    }

    private GoalInvitation getInvitation(
            UUID invitationId) {

        return goalInvitationRepository
                .findById(invitationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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

    private String getUserDisplayName(
            UserLookupPort.UserInfo user) {

        return user.username() != null
                ? user.username()
                : user.email();
    }
}