package com.puntomartinez.millete.groupgoals.domain.ports.out;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import java.util.List;
import java.util.UUID;

public interface GoalContributionRepository {
    GoalContribution save(GoalContribution contribution);
    List<GoalContribution> findByGoalId(UUID goalId);
    List<GoalContribution> findByGoalId(UUID goalId, int page, int size);
    long countByGoalId(UUID goalId);
}
