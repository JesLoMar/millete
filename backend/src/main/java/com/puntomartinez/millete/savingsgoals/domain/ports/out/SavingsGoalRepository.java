package com.puntomartinez.millete.savingsgoals.domain.ports.out;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavingsGoalRepository {
    SavingsGoal save(SavingsGoal savingsGoal);
    Optional<SavingsGoal> findById(UUID id);
    Optional<SavingsGoal> findByIdAndUserId(UUID id, UUID userId);
    List<SavingsGoal> findAllByUserId(UUID userId);
    List<SavingsGoal> findAllByUserIdAndStatus(UUID userId, String status);
    List<SavingsGoal> findAllByUserId(UUID userId, int page, int size, String search, String status);
    long countByUserIdAndFilters(UUID userId, String search, String status);
}
