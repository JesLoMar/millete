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
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupGoalInvitationService implements
        InviteMemberUseCase,
        AcceptInvitationUseCase,
        RejectInvitationUseCase,
        ListPendingInvitationsUseCase {

    private final GoalInvitationRepository goalInvitationRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalUnitRepository goalUnitRepository;
    private final UserLookupPort userLookupPort;
    private final GoalInvitationNotificationPort notificationPort;
    private final TimeProvider timeProvider;

    public GroupGoalInvitationService(
            GoalInvitationRepository goalInvitationRepository,
            GoalMemberRepository goalMemberRepository,
            GoalUnitRepository goalUnitRepository,
            UserLookupPort userLookupPort,
            GoalInvitationNotificationPort notificationPort,
            TimeProvider timeProvider
    ) {
        this.goalInvitationRepository = goalInvitationRepository;
        this.goalMemberRepository = goalMemberRepository;
        this.goalUnitRepository = goalUnitRepository;
        this.userLookupPort = userLookupPort;
        this.notificationPort = notificationPort;
        this.timeProvider = timeProvider;
    }

    @Override
    @Transactional
    public InviteMemberUseCase.InvitationResult inviteMember(
            UUID goalId,
            UUID inviterUserId,
            InviteMemberUseCase.InviteMemberCommand command
    ) {
        GoalUnit goal = goalUnitRepository.findById(goalId)
                .filter(GoalUnit::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Objetivo no encontrado"
                        )
                );

        requireAdmin(goalId, inviterUserId);

        UserLookupPort.UserInfo invitedUser = userLookupPort
                .findByIdentifier(command.identifier())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado con ese identificador"
                        )
                );

        UUID invitedUserId = invitedUser.id();

        if (invitedUserId.equals(inviterUserId)) {
            throw new InvalidInputException(
                    "No puedes invitarte a ti mismo"
            );
        }

        goalMemberRepository.findByGoalIdAndUserId(goalId, invitedUserId)
                .ifPresent(m -> {
                    if (m.isActive()) {
                        throw new ResourceAlreadyExistsException(
                                "El usuario ya pertenece a este objetivo"
                        );
                    }
                });

        goalInvitationRepository
                .findByGoalIdAndInvitedUserIdAndStatus(
                        goalId,
                        invitedUserId,
                        InvitationStatus.PENDING
                )
                .ifPresent(i -> {
                    throw new ResourceAlreadyExistsException(
                            "Ya existe una invitación pendiente para este usuario"
                    );
                });

        GoalInvitation invitation = GoalInvitation.create(
                timeProvider,
                goalId,
                inviterUserId,
                invitedUserId
        );

        GoalInvitation saved = goalInvitationRepository.save(invitation);

        UserLookupPort.UserInfo inviterUser = userLookupPort
                .findById(inviterUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario invitador no encontrado"
                        )
                );

        notificationPort.createInvitationNotification(
        saved,
        goal.getName(),
        inviterUser.username() != null
                ? inviterUser.username()
                : inviterUser.email()
        );

        return new InviteMemberUseCase.InvitationResult(
                saved.getId(),
                saved.getGoalId(),
                goal.getName(),
                inviterUserId,
                inviterUser.username() != null
                        ? inviterUser.username()
                        : inviterUser.email(),
                invitedUserId,
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public GoalInvitation acceptInvitation(
            UUID userId,
            UUID invitationId
    ) {
        GoalInvitation invitation = getInvitation(invitationId);
        validateInvitedUser(invitation, userId);

        if (!invitation.isAcceptable(timeProvider)) {
            throw new ForbiddenOperationException(
                    "La invitación ya no es válida"
            );
        }

        goalUnitRepository.findById(invitation.getGoalId())
                .filter(GoalUnit::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "El objetivo ya no existe"
                        )
                );

        GoalMember existingMember = goalMemberRepository
                .findByGoalIdAndUserId(
                        invitation.getGoalId(),
                        userId
                )
                .orElse(null);

        if (existingMember == null) {
            GoalMember member = GoalMember.create(
                    timeProvider,
                    invitation.getGoalId(),
                    userId,
                    GoalRole.MEMBER,
                    null
            );
            goalMemberRepository.save(member);
        } else {
            if (existingMember.isActive()) {
                throw new ForbiddenOperationException(
                        "El usuario ya pertenece a este objetivo"
                );
            }
            existingMember.activate(timeProvider);
            goalMemberRepository.save(existingMember);
        }

        invitation.markAsAccepted(timeProvider);
        GoalInvitation savedInvitation =
                goalInvitationRepository.save(invitation);

        notificationPort.markInvitationNotificationAsActioned(
                userId,
                invitationId
        );

        return savedInvitation;
    }

    @Override
    @Transactional
    public void rejectInvitation(UUID userId, UUID invitationId) {
        GoalInvitation invitation = getInvitation(invitationId);
        validateInvitedUser(invitation, userId);

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ForbiddenOperationException(
                    "La invitación ya no está pendiente"
            );
        }

        invitation.markAsRejected(timeProvider);
        goalInvitationRepository.save(invitation);

        notificationPort.markInvitationNotificationAsActioned(
                userId,
                invitationId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListPendingInvitationsUseCase.InvitationResult> listPendingInvitations(
            UUID userId
    ) {
        List<GoalInvitation> invitations = goalInvitationRepository
                .findActiveAndNotExpiredByInvitedUserIdAndStatus(
                        userId,
                        InvitationStatus.PENDING
                );

        if (invitations.isEmpty()) {
            return List.of();
        }

        List<UUID> goalIds = invitations.stream()
                .map(GoalInvitation::getGoalId)
                .distinct()
                .toList();

        List<UUID> inviterIds = invitations.stream()
                .map(GoalInvitation::getInviterUserId)
                .distinct()
                .toList();

        Map<UUID, GoalUnit> goalsById = goalUnitRepository
                .findByIds(goalIds)
                .stream()
                .collect(Collectors.toMap(
                        GoalUnit::getId,
                        g -> g,
                        (a, b) -> a
                ));

        Map<UUID, UserLookupPort.UserInfo> invitersById =
                userLookupPort.findByIds(inviterIds);

        return invitations.stream()
                .map(invitation -> {
                    GoalUnit goal = goalsById.get(
                            invitation.getGoalId()
                    );
                    UserLookupPort.UserInfo inviter = invitersById.get(
                            invitation.getInviterUserId()
                    );

                    String inviterName = inviter != null
                            ? (inviter.username() != null
                            ? inviter.username()
                            : inviter.email())
                            : "Usuario desconocido";

                    return new ListPendingInvitationsUseCase.InvitationResult(
                            invitation.getId(),
                            invitation.getGoalId(),
                            goal != null ? goal.getName() : "Objetivo eliminado",
                            invitation.getInviterUserId(),
                            inviterName,
                            invitation.getInvitedUserId(),
                            invitation.getStatus(),
                            invitation.getCreatedAt()
                    );
                })
                .toList();
    }

    private GoalInvitation getInvitation(UUID invitationId) {
        return goalInvitationRepository.findById(invitationId)
                .filter(GoalInvitation::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invitación no encontrada"
                        )
                );
    }

    private void validateInvitedUser(
            GoalInvitation invitation,
            UUID userId
    ) {
        if (!invitation.getInvitedUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "No puedes actuar sobre esta invitación"
            );
        }
    }

    private void requireAdmin(UUID goalId, UUID userId) {
        GoalMember member = goalMemberRepository
                .findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "No tienes permisos sobre este objetivo"
                        )
                );

        if (!member.isActive() || !member.isAdmin()) {
            throw new ForbiddenOperationException(
                    "Solo los administradores pueden invitar miembros"
            );
        }
    }
}