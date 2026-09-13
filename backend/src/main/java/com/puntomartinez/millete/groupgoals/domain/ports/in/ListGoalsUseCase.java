package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ListGoalsUseCase {

    GoalsPage listGoals(
            UUID userId,
            int page,
            int size
    );

    record GoalSummary(
            UUID id,
            String name,
            BigDecimal monthlyTarget,
            long activeMembers,
            boolean admin
    ) {
    }

    record GoalsPage(
            List<GoalSummary> goals,
            long totalElements,
            int totalPages
    ) {
    }
}