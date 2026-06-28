package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;

public interface UpdateSavingsGoalUseCase {
    SavingsGoal update(UpdateSavingsGoalCommand command);
}