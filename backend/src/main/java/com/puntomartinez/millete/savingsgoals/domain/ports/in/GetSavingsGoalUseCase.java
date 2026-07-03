package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import java.util.UUID;

public interface GetSavingsGoalUseCase {
    SavingsGoal getByIdAndUserId(UUID id, UUID userId);
}
