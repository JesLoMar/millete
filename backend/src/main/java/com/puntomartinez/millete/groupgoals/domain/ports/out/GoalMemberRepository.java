package com.puntomartinez.millete.groupgoals.domain.ports.out;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalMemberRepository {
    GoalMember save(GoalMember goalMember);
    Optional<GoalMember> findById(UUID id);
    Optional<GoalMember> findByGoalIdAndUserId(UUID goalId, UUID userId);
    List<GoalMember> findByGoalId(UUID goalId);
    List<GoalMember> findByUserId(UUID userId);
    List<GoalMember> findByGoalIdIn(Collection<UUID> goalIds);
}
