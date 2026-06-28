package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.util.UUID;

public interface DeleteGoalUnitUseCase {
    void deleteGoalUnit(UUID goalId, UUID userId);
}