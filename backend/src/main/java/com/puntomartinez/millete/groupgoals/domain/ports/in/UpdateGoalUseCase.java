package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateGoalUseCase {

    void update(
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