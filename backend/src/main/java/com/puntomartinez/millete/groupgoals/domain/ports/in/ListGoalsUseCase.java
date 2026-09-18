package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ListGoalsUseCase {

    List<GoalSummary> listGoals(UUID userId);

    record GoalSummary(
            UUID id,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode,
            boolean admin,
            LocalDateTime createdAt
    ) {
    }
}