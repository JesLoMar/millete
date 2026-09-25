package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository.MemberContributionTotals;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GetGoalDetailUseCase {

    GoalDetail getGoalDetail(UUID goalId, UUID userId);

    record MemberDetail(
            UUID id,
            UUID userId,
            String username,
            String email,
            GoalRole role,
            BigDecimal salary,
            BigDecimal customPercentage,
            Instant joinedAt
    ) {
    }

    record GoalDetail(
            UUID id,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode,
            Instant createdAt,
            List<MemberDetail> members,
            List<MemberContributionTotals> totals
    ) {
    }
}