package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.in.CalculateContributionsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetContributionHistoryUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetGoalDetailUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.ListGoalsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.UserLookupPort;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupGoalQueryService implements
        CalculateContributionsUseCase,
        ListGoalsUseCase,
        GetGoalDetailUseCase,
        GetContributionHistoryUseCase {

    private final GoalUnitRepository goalUnitRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final UserLookupPort userLookupPort;

    @Override
    public Map<UUID, BigDecimal> calculateContributions(
            UUID goalId,
            UUID callerId) {

        getMember(goalId, callerId);

        GoalUnit goalUnit = getGoal(goalId);

        List<GoalMember> members =
                goalMemberRepository.findByGoalId(goalId);

        return goalUnit.calculateContributions(members);
    }

    @Override
    public GoalsPage listGoals(
            UUID userId,
            int page,
            int size) {

        long totalElements =
                goalUnitRepository.countByUserId(userId);

        int totalPages =
                calculateTotalPages(totalElements, size);

        if (totalElements == 0) {
            return new GoalsPage(
                    List.of(),
                    0,
                    0
            );
        }

        int safePage = Math.min(
                Math.max(page, 0),
                totalPages - 1
        );

        List<GoalUnit> goals =
                goalUnitRepository.findByUserId(
                        userId,
                        safePage,
                        size
                );

        List<UUID> goalIds = goals.stream()
                .map(GoalUnit::getId)
                .toList();

        List<GoalMember> members =
                goalMemberRepository.findByGoalIdIn(goalIds);

        Map<UUID, List<GoalMember>> membersByGoal =
                new HashMap<>();

        for (GoalMember member : members) {
            membersByGoal
                    .computeIfAbsent(
                            member.getGoalId(),
                            ignored -> new ArrayList<>()
                    )
                    .add(member);
        }

        List<GoalSummary> summaries = goals.stream()
                .map(goal -> {

                    List<GoalMember> goalMembers =
                            membersByGoal.getOrDefault(
                                    goal.getId(),
                                    List.of()
                            );

                    boolean admin = goalMembers.stream()
                            .anyMatch(member ->
                                    member.getUserId().equals(userId)
                                            && member.isAdmin());

                    return new GoalSummary(
                            goal.getId(),
                            goal.getName(),
                            goal.getMonthlyTarget(),
                            goalMembers.size(),
                            admin
                    );
                })
                .sorted((left, right) -> {
                    if (left.admin() != right.admin()) {
                        return Boolean.compare(
                                right.admin(),
                                left.admin()
                        );
                    }

                    return left.name()
                            .compareToIgnoreCase(right.name());
                })
                .toList();

        return new GoalsPage(
                summaries,
                totalElements,
                totalPages
        );
    }

    @Override
    public GoalDetail getGoalDetail(
            UUID goalId,
            UUID userId) {

        GoalUnit goalUnit =
                getGoal(goalId);

        GoalMember requester =
                getMember(goalId, userId);

        List<GoalMember> members =
                goalMemberRepository.findByGoalId(goalId);

        List<GoalContribution> contributions =
                goalContributionRepository.findByGoalId(goalId);

        Map<UUID, UserLookupPort.UserInfo> usersById =
                findUsersForGoal(members, contributions);

        List<GetGoalDetailUseCase.Member> memberResults =
                members.stream()
                        .map(member ->
                                new GetGoalDetailUseCase.Member(
                                        member.getId(),
                                        member.getUserId(),
                                        resolveUserName(
                                                member.getUserId(),
                                                usersById
                                        ),
                                        member.getRole().name(),
                                        member.getSalary(),
                                        member.getCustomPercentage()
                                ))
                        .toList();

        List<GetGoalDetailUseCase.Contribution> contributionResults =
                contributions.stream()
                        .map(contribution ->
                                new GetGoalDetailUseCase.Contribution(
                                        contribution.getId(),
                                        contribution.getUserId(),
                                        resolveUserName(
                                                contribution.getUserId(),
                                                usersById
                                        ),
                                        contribution.getAmount(),
                                        contribution.getDate()
                                ))
                        .toList();

        Map<UUID, BigDecimal> totals =
                new HashMap<>();

        for (GoalContribution contribution : contributions) {
            totals.merge(
                    contribution.getUserId(),
                    contribution.getAmount(),
                    BigDecimal::add
            );
        }

        return new GoalDetail(
                goalUnit.getId(),
                goalUnit.getName(),
                goalUnit.getMonthlyTarget(),
                goalUnit.getDistributionMode().name(),
                requester.isAdmin(),
                memberResults,
                contributionResults,
                totals
        );
    }

    @Override
    public ContributionHistory getContributionHistory(
            UUID goalId,
            UUID userId,
            int page,
            int size) {

        getMember(goalId, userId);

        long totalElements =
                goalContributionRepository.countByGoalId(goalId);

        int totalPages =
                calculateTotalPages(totalElements, size);

        if (totalElements == 0) {
            return new ContributionHistory(
                    List.of(),
                    0,
                    0
            );
        }

        int safePage = Math.min(
                Math.max(page, 0),
                totalPages - 1
        );

        List<GoalContribution> contributions =
                goalContributionRepository.findByGoalId(
                        goalId,
                        safePage,
                        size
                );

        Set<UUID> userIds = contributions.stream()
                .map(GoalContribution::getUserId)
                .collect(java.util.stream.Collectors.toSet());

        Map<UUID, UserLookupPort.UserInfo> usersById =
                userLookupPort.findByIds(userIds);

        List<GetContributionHistoryUseCase.Contribution> result =
                contributions.stream()
                        .map(contribution ->
                                new GetContributionHistoryUseCase.Contribution(
                                        contribution.getId(),
                                        contribution.getUserId(),
                                        resolveUserName(
                                                contribution.getUserId(),
                                                usersById
                                        ),
                                        contribution.getAmount(),
                                        contribution.getDate()
                                ))
                        .toList();

        return new ContributionHistory(
                result,
                totalElements,
                totalPages
        );
    }

    private Map<UUID, UserLookupPort.UserInfo> findUsersForGoal(
            List<GoalMember> members,
            List<GoalContribution> contributions) {

        Set<UUID> userIds = new HashSet<>();

        members.forEach(member ->
                userIds.add(member.getUserId())
        );

        contributions.forEach(contribution ->
                userIds.add(contribution.getUserId())
        );

        return userLookupPort.findByIds(userIds);
    }

    private String resolveUserName(
            UUID userId,
            Map<UUID, UserLookupPort.UserInfo> usersById) {

        UserLookupPort.UserInfo user =
                usersById.get(userId);

        if (user == null || user.username() == null) {
            return "Usuario";
        }

        return user.username();
    }

    private GoalUnit getGoal(UUID goalId) {
        return goalUnitRepository.findById(goalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "La meta no existe."
                        ));
    }

    private GoalMember getMember(
            UUID goalId,
            UUID userId) {

        GoalMember member =
                goalMemberRepository
                        .findByGoalIdAndUserId(goalId, userId)
                        .orElseThrow(() ->
                                new ForbiddenOperationException(
                                        "El usuario no pertenece a esta meta."
                                ));

        if (!member.isActive()) {
            throw new ForbiddenOperationException(
                    "El usuario no pertenece activamente a esta meta."
            );
        }

        return member;
    }

    private int calculateTotalPages(
            long totalElements,
            int size) {

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe ser mayor que cero."
            );
        }

        return (int) Math.ceil(
                (double) totalElements / size
        );
    }
}