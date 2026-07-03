package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import java.util.List;
import java.util.UUID;

public interface ListSavingsGoalsUseCase {
    List<SavingsGoal> findByUserId(UUID userId);
    List<SavingsGoal> findByUserIdAndStatus(UUID userId, String status);
}
