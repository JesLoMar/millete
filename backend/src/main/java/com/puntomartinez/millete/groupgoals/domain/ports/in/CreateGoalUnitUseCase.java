package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreateGoalUnitUseCase {

    GoalUnit create(CreateGoalUnitCommand command);

    record CreateGoalUnitCommand(
            UUID creatorUserId,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode
    ) {
    }
}