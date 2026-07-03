package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaGoalMemberRepository extends JpaRepository<GoalMemberEntity, UUID> {
    Optional<GoalMemberEntity> findByGoalIdAndUserId(UUID goalId, UUID userId);
    List<GoalMemberEntity> findByGoalIdAndActiveTrue(UUID goalId);
    List<GoalMemberEntity> findByUserIdAndActiveTrue(UUID userId);
    void deleteByGoalIdAndUserId(UUID goalId, UUID userId);
}
