package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;

public interface AddContributionToGoalUseCase {
    SavingsGoal addContribution(AddContributionToGoalCommand command);
}
