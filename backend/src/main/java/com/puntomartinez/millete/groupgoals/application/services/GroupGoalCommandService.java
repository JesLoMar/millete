package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.in.*;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;

import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupGoalCommandService implements
        CreateGoalUnitUseCase,
        UpdateGoalUseCase,
        DeleteGoalUnitUseCase,
        UpdateMemberUseCase,
        DeleteMemberUseCase,
        AddContributionUseCase {

    private final GoalUnitRepository goalUnitRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalContributionRepository goalContributionRepository;

    @Override
    @Transactional
    public GoalUnit create(UUID adminUserId, CreateGoalUnitCommand command) {
        GoalUnit goalUnit = GoalUnit.create(
                command.name(),
                command.monthlyTarget(),
                command.distributionMode()
        );

        goalUnitRepository.save(goalUnit);

        GoalMember adminMember = GoalMember.create(
                goalUnit.getId(),
                adminUserId,
                GoalRole.ADMIN,
                null
        );

        goalMemberRepository.save(adminMember);

        return goalUnit;
    }

    @Override
    @Transactional
    public void update(
            UUID goalId,
            UUID userId,
            UpdateGoalCommand command) {

        GoalMember requester = getMember(goalId, userId);

        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException(
                    "Solo un administrador puede modificar la meta."
            );
        }

        GoalUnit goalUnit = getGoal(goalId);

        goalUnit.updateDetails(
                command.name(),
                command.monthlyTarget(),
                command.distributionMode()
        );

        goalUnitRepository.save(goalUnit);
    }

    @Override
    @Transactional
    public void deleteMember(
            UUID goalId,
            UUID memberId,
            UUID userId) {

        GoalMember requester = getMember(goalId, userId);

        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException(
                    "Solo un administrador puede eliminar miembros."
            );
        }

        GoalMember member = goalMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El miembro no existe."
                ));

        if (!goalId.equals(member.getGoalId())) {
            throw new ForbiddenOperationException(
                    "El miembro no pertenece a esta meta."
            );
        }

        if (member.isAdmin()) {
            long adminCount = goalMemberRepository.findByGoalId(goalId)
                    .stream()
                    .filter(GoalMember::isAdmin)
                    .count();

            if (adminCount <= 1) {
                throw new ForbiddenOperationException(
                        "No se puede eliminar al último administrador."
                );
            }
        }

        member.deactivate();
        goalMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateMember(
            UUID goalId,
            UUID memberId,
            UUID userId,
            UpdateMemberCommand command) {

        GoalMember requester = getMember(goalId, userId);

        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException(
                    "Solo un administrador puede modificar miembros."
            );
        }

        GoalMember member = goalMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El miembro no existe."
                ));

        if (!goalId.equals(member.getGoalId())) {
            throw new ForbiddenOperationException(
                    "El miembro no pertenece a esta meta."
            );
        }

        if (member.isAdmin()
                && command.role() == GoalRole.MEMBER) {

            long adminCount = goalMemberRepository.findByGoalId(goalId)
                    .stream()
                    .filter(GoalMember::isAdmin)
                    .count();

            if (adminCount <= 1) {
                throw new ForbiddenOperationException(
                        "No se puede quitar el rol al último administrador."
                );
            }
        }

        member.updateDetails(
                command.role(),
                command.salary(),
                command.customPercentage()
        );

        goalMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void addContribution(
            UUID goalId,
            UUID userId,
            AddContributionCommand command) {

        getMember(goalId, userId);

        GoalContribution contribution = GoalContribution.create(
                goalId,
                userId,
                command.amount()
        );

        goalContributionRepository.save(contribution);
    }

    @Override
    @Transactional
    public void deleteGoalUnit(
            UUID goalId,
            UUID userId) {

        GoalMember requester = getMember(goalId, userId);

        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException(
                    "Solo un administrador puede eliminar la meta."
            );
        }

        GoalUnit goalUnit = getGoal(goalId);

        goalUnit.deactivate();
        goalUnitRepository.save(goalUnit);

        for (GoalMember member : goalMemberRepository.findByGoalId(goalId)) {
            member.deactivate();
            goalMemberRepository.save(member);
        }
    }

    private GoalUnit getGoal(UUID goalId) {
        return goalUnitRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La meta no existe."
                ));
    }

    private GoalMember getMember(
            UUID goalId,
            UUID userId) {

        GoalMember member = goalMemberRepository
                .findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "El usuario no pertenece a esta meta."
                ));

        if (!member.isActive()) {
            throw new ForbiddenOperationException(
                    "El usuario no pertenece activamente a esta meta."
            );
        }

        return member;
    }
}