package com.puntomartinez.millete.groupgoals.domain.ports.out;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetContributionHistoryUseCase.PaginatedContributions;

import java.util.List;
import java.util.UUID;

public interface GoalContributionRepository {

    GoalContribution save(GoalContribution contribution);

    PaginatedContributions findByGoalId(
            UUID goalId,
            int page,
            int size
    );

    long countByGoalId(UUID goalId);

    List<MemberContributionTotals> sumByGoalId(UUID goalId);

    void deactivateByGoalId(UUID goalId);

    record MemberContributionTotals(
            UUID userId,
            java.math.BigDecimal totalDeposits,
            java.math.BigDecimal totalWithdrawals,
            java.math.BigDecimal net
    ) {
    }
}