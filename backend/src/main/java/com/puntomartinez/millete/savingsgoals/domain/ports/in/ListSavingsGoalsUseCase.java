package com.puntomartinez.millete.savingsgoals.domain.ports.in;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import java.util.List;
import java.util.UUID;

public interface ListSavingsGoalsUseCase {
    List<SavingsGoal> findByUserId(UUID userId);
    List<SavingsGoal> findByUserIdAndStatus(UUID userId, String status);
    List<SavingsGoal> findByUserId(UUID userId, int page, int size, String search, String status);
    long countByUserIdAndFilters(UUID userId, String search, String status);
}
