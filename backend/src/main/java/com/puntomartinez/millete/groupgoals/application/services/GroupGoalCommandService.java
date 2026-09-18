package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.ContributionType;
import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.in.*;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalInvitationRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GroupGoalCommandService implements
        CreateGoalUnitUseCase,
        UpdateGoalUseCase,
        DeleteGoalUnitUseCase,
        UpdateMemberUseCase,
        DeleteMemberUseCase,
        AddContributionUseCase,
        WithdrawContributionUseCase,
        LeaveGoalUseCase {

    private final GoalUnitRepository goalUnitRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final GoalInvitationRepository goalInvitationRepository;

    public GroupGoalCommandService(
            GoalUnitRepository goalUnitRepository,
            GoalMemberRepository goalMemberRepository,
            GoalContributionRepository goalContributionRepository,
            GoalInvitationRepository goalInvitationRepository
    ) {
        this.goalUnitRepository = goalUnitRepository;
        this.goalMemberRepository = goalMemberRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.goalInvitationRepository = goalInvitationRepository;
    }

    @Override
    @Transactional
    public GoalUnit create(CreateGoalUnitCommand command) {
        GoalUnit goal = GoalUnit.create(
                command.name(),
                command.monthlyTarget(),
                command.distributionMode()
        );

        GoalUnit saved = goalUnitRepository.save(goal);

        GoalMember admin = GoalMember.create(
                saved.getId(),
                command.creatorUserId(),
                GoalRole.ADMIN,
                null
        );
        goalMemberRepository.save(admin);

        return saved;
    }

    @Override
    @Transactional
    public GoalUnit update(
            UUID goalId,
            UUID userId,
            UpdateGoalCommand command
    ) {
        GoalUnit goal = getActiveGoal(goalId);
        requireAdmin(goalId, userId);

        goal.updateDetails(
                command.name(),
                command.monthlyTarget(),
                command.distributionMode()
        );

        return goalUnitRepository.save(goal);
    }

    @Override
    @Transactional
    public void deleteGoalUnit(UUID goalId, UUID userId) {
        getActiveGoal(goalId);
        requireAdmin(goalId, userId);

        goalUnitRepository.findById(goalId).ifPresent(goal -> {
            goal.deactivate();
            goalUnitRepository.save(goal);
        });

        goalMemberRepository.deactivateByGoalId(goalId);
        goalInvitationRepository.deactivatePendingByGoalId(goalId);
        goalContributionRepository.deactivateByGoalId(goalId);
    }

    @Override
    @Transactional
    public GoalMember updateMember(
            UUID goalId,
            UUID memberId,
            UUID requesterUserId,
            UpdateMemberCommand command
    ) {
        getActiveGoal(goalId);
        requireAdmin(goalId, requesterUserId);

        GoalMember member = goalMemberRepository.findById(memberId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Miembro no encontrado")
                );

        if (!member.getGoalId().equals(goalId)) {
            throw new ForbiddenOperationException(
                    "El miembro no pertenece a este objetivo"
            );
        }

        if (!member.isActive()) {
            throw new ResourceNotFoundException("Miembro no encontrado");
        }

        validateDistributionConsistency(goalId, command);

        member.updateDetails(
                command.role(),
                command.salary(),
                command.customPercentage()
        );

        return goalMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void deleteMember(
            UUID goalId,
            UUID memberId,
            UUID requesterUserId
    ) {
        getActiveGoal(goalId);
        requireAdmin(goalId, requesterUserId);

        GoalMember member = goalMemberRepository.findById(memberId)
                .orElse(null);

        if (member == null
                || !member.getGoalId().equals(goalId)
                || !member.isActive()) {
            return;
        }

        preventLastAdminRemoval(goalId, member);

        member.deactivate();
        goalMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void addContribution(
            UUID goalId,
            UUID userId,
            AddContributionCommand command
    ) {
        getActiveGoal(goalId);
        requireActiveMember(goalId, userId);

        GoalContribution contribution = GoalContribution.create(
                goalId,
                userId,
                command.amount(),
                ContributionType.DEPOSIT,
                LocalDateTime.now()
        );

        goalContributionRepository.save(contribution);
    }

    @Override
    @Transactional
    public void withdrawContribution(
            UUID goalId,
            UUID userId,
            WithdrawContributionCommand command
    ) {
        getActiveGoal(goalId);
        requireActiveMember(goalId, userId);

        GoalContribution contribution = GoalContribution.create(
                goalId,
                userId,
                command.amount(),
                ContributionType.WITHDRAWAL,
                LocalDateTime.now()
        );

        goalContributionRepository.save(contribution);
    }

    @Override
    @Transactional
    public void leaveGoal(UUID goalId, UUID userId) {
        getActiveGoal(goalId);

        GoalMember member = goalMemberRepository
                .findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No perteneces a este objetivo"
                        )
                );

        if (!member.isActive()) {
            throw new ResourceNotFoundException(
                    "No perteneces a este objetivo"
            );
        }

        preventLastAdminRemoval(goalId, member);

        member.deactivate();
        goalMemberRepository.save(member);
    }

    private GoalUnit getActiveGoal(UUID goalId) {
        return goalUnitRepository.findById(goalId)
                .filter(GoalUnit::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Objetivo no encontrado"
                        )
                );
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
                    "Solo los administradores pueden realizar esta acción"
            );
        }
    }

    private void requireActiveMember(UUID goalId, UUID userId) {
        GoalMember member = goalMemberRepository
                .findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "No perteneces a este objetivo"
                        )
                );

        if (!member.isActive()) {
            throw new ForbiddenOperationException(
                    "No perteneces a este objetivo"
            );
        }
    }

    private void preventLastAdminRemoval(UUID goalId, GoalMember member) {
        if (!member.isAdmin()) {
            return;
        }

        long activeAdmins = goalMemberRepository.findActiveByGoalId(goalId)
                .stream()
                .filter(GoalMember::isAdmin)
                .count();

        if (activeAdmins <= 1) {
            throw new InvalidInputException(
                    "No se puede eliminar al último administrador del objetivo"
            );
        }
    }

    private void validateDistributionConsistency(
            UUID goalId,
            UpdateMemberCommand command
    ) {
        GoalUnit goal = goalUnitRepository.findById(goalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Objetivo no encontrado"
                        )
                );

        DistributionMode mode = goal.getDistributionMode();

        if (mode == DistributionMode.CUSTOM
                && command.customPercentage() == null) {
            throw new InvalidInputException(
                    "En modo CUSTOM todos los miembros deben tener un porcentaje asignado"
            );
        }
    }
}