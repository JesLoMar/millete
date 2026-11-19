package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.util.UUID;

public interface ResetGoalContributionsUseCase {
    void resetContributions(UUID goalId, UUID userId);
}
