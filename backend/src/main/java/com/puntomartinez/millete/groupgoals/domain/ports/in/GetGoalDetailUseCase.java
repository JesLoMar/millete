package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GetGoalDetailUseCase {

    GoalDetail getGoalDetail(
            UUID goalId,
            UUID userId
    );

    record Member(
            UUID id,
            UUID userId,
            String memberName,
            String role,
            BigDecimal salary,
            BigDecimal customPercentage
    ) {
    }

    record GoalDetail(
            UUID id,
            String name,
            BigDecimal monthlyTarget,
            String distributionMode,
            boolean admin,
            List<Member> members,
            Map<UUID, BigDecimal> contributionTotals
    ) {
    }
}