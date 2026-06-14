package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;

public interface CreateSavingsGoalUseCase {
    SavingsGoal create(CreateSavingsGoalCommand command);
}