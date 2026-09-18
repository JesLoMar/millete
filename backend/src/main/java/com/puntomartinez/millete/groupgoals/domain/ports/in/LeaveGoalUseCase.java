package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.util.UUID;

public interface LeaveGoalUseCase {

    void leaveGoal(UUID goalId, UUID userId);
}