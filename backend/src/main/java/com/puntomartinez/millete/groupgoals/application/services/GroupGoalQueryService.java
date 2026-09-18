package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.in.CalculateContributionsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetContributionHistoryUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetGoalDetailUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.ListGoalsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository.MemberContributionTotals;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.UserLookupPort;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupGoalQueryService implements
        ListGoalsUseCase,
        GetGoalDetailUseCase,
        GetContributionHistoryUseCase,
        CalculateContributionsUseCase {

    private final GoalUnitRepository goalUnitRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final UserLookupPort userLookupPort;

    public GroupGoalQueryService(
            GoalUnitRepository goalUnitRepository,
            GoalMemberRepository goalMemberRepository,
            GoalContributionRepository goalContributionRepository,
            UserLookupPort userLookupPort
    ) {
        this.goalUnitRepository = goalUnitRepository;
        this.goalMemberRepository = goalMemberRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.userLookupPort = userLookupPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalSummary> listGoals(UUID userId) {
        List<GoalMember> memberships = goalMemberRepository
                .findActiveByUserId(userId);

        if (memberships.isEmpty()) {
            return List.of();
        }

        List<UUID> goalIds = memberships.stream()
                .map(GoalMember::getGoalId)
                .toList();

        List<GoalUnit> goals = goalUnitRepository.findByIds(goalIds);

        Map<UUID, GoalMember> memberByGoal = memberships.stream()
                .collect(Collectors.toMap(
                        GoalMember::getGoalId, m -> m, (a, b) -> a
                ));

        return goals.stream()
                .filter(GoalUnit::isActive)
                .map(goal -> {
                    GoalMember membership = memberByGoal.get(goal.getId());
                    return new GoalSummary(
                            goal.getId(),
                            goal.getName(),
                            goal.getMonthlyTarget(),
                            goal.getDistributionMode(),
                            membership != null && membership.isAdmin(),
                            goal.getCreatedAt()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GoalDetail getGoalDetail(UUID goalId, UUID userId) {
        GoalUnit goal = goalUnitRepository.findById(goalId)
                .filter(GoalUnit::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Objetivo no encontrado")
                );

        GoalMember requester = goalMemberRepository
                .findByGoalIdAndUserId(goalId, userId)
                .filter(GoalMember::isActive)
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "No perteneces a este objetivo"
                        )
                );

        List<GoalMember> members = goalMemberRepository
                .findActiveByGoalId(goalId);

        List<UUID> memberUserIds = members.stream()
                .map(GoalMember::getUserId)
                .toList();

        Map<UUID, UserLookupPort.UserInfo> usersById =
                userLookupPort.findByIds(memberUserIds);

        boolean requesterIsAdmin = requester.isAdmin();

        List<MemberDetail> memberDetails = members.stream()
                .map(member -> {
                    UserLookupPort.UserInfo userInfo = usersById.get(
                            member.getUserId()
                    );
                    boolean canSeeFinancialData = requesterIsAdmin
                            || member.getUserId().equals(userId);

                    return new MemberDetail(
                            member.getId(),
                            member.getUserId(),
                            userInfo != null ? userInfo.username() : null,
                            userInfo != null ? userInfo.email() : null,
                            member.getRole(),
                            canSeeFinancialData ? member.getSalary() : null,
                            canSeeFinancialData ? member.getCustomPercentage() : null,
                            member.getJoinedAt()
                    );
                })
                .toList();

        List<MemberContributionTotals> totals =
                goalContributionRepository.sumByGoalId(goalId);

        return new GoalDetail(
                goal.getId(),
                goal.getName(),
                goal.getMonthlyTarget(),
                goal.getDistributionMode(),
                goal.getCreatedAt(),
                memberDetails,
                totals
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedContributions getContributionHistory(
            UUID goalId, UUID userId, int page, int size
    ) {
        requireActiveMember(goalId, userId);
        return goalContributionRepository.findByGoalId(goalId, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberContributionTotals> getTotalsByMember(
            UUID goalId, UUID userId
    ) {
        requireActiveMember(goalId, userId);
        return goalContributionRepository.sumByGoalId(goalId);
    }

    @Override
    @Transactional(readOnly = true)
    public ContributionsCalculation calculateContributions(
            UUID goalId, UUID userId
    ) {
        GoalUnit goal = goalUnitRepository.findById(goalId)
                .filter(GoalUnit::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Objetivo no encontrado")
                );

        requireActiveMember(goalId, userId);

        List<GoalMember> members = goalMemberRepository
                .findActiveByGoalId(goalId);

        BigDecimal target = goal.getMonthlyTarget();
        DistributionMode mode = goal.getDistributionMode();

        List<MemberContribution> contributions = switch (mode) {
            case EQUITATIVE -> calculateEquitative(members, target);
            case PROPORTIONAL -> calculateProportional(members, target);
            case CUSTOM -> calculateCustom(members, target);
        };

        return new ContributionsCalculation(target, mode, contributions);
    }

    private List<MemberContribution> calculateEquitative(
            List<GoalMember> members, BigDecimal target
    ) {
        if (members.isEmpty()) {
            return List.of();
        }
        BigDecimal perMember = target.divide(
                new BigDecimal(members.size()), 2, RoundingMode.HALF_UP
        );
        return members.stream()
                .map(m -> new MemberContribution(m.getUserId(), perMember))
                .toList();
    }

    private List<MemberContribution> calculateProportional(
            List<GoalMember> members, BigDecimal target
    ) {
        BigDecimal totalSalary = members.stream()
                .map(m -> m.getSalary() != null ? m.getSalary() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSalary.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidInputException(
                    "No se puede calcular el reparto proporcional "
                            + "porque ningún miembro tiene salario asignado"
            );
        }

        return members.stream()
                .map(m -> {
                    BigDecimal salary = m.getSalary() != null
                            ? m.getSalary() : BigDecimal.ZERO;
                    BigDecimal share = target
                            .multiply(salary)
                            .divide(totalSalary, 2, RoundingMode.HALF_UP);
                    return new MemberContribution(m.getUserId(), share);
                })
                .toList();
    }

    private List<MemberContribution> calculateCustom(
            List<GoalMember> members, BigDecimal target
    ) {
        List<MemberContribution> result = new ArrayList<>();
        BigDecimal totalPercentage = BigDecimal.ZERO;

        for (GoalMember member : members) {
            BigDecimal percentage = member.getCustomPercentage();
            if (percentage == null) {
                throw new InvalidInputException(
                        "En modo CUSTOM todos los miembros deben tener "
                                + "un porcentaje asignado"
                );
            }
            totalPercentage = totalPercentage.add(percentage);
            BigDecimal share = target
                    .multiply(percentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            result.add(new MemberContribution(member.getUserId(), share));
        }

        if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
            throw new InvalidInputException(
                    "Los porcentajes personalizados deben sumar 100. "
                            + "Actual: " + totalPercentage
            );
        }
        return result;
    }

    private void requireActiveMember(UUID goalId, UUID userId) {
        goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
                .filter(GoalMember::isActive)
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "No perteneces a este objetivo"
                        )
                );
    }
}