package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateGoalUseCase {

    GoalUnit update(
            UUID goalId,
            UUID userId,
            UpdateGoalCommand command
    );

    record UpdateGoalCommand(
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode
    ) {
    }
}