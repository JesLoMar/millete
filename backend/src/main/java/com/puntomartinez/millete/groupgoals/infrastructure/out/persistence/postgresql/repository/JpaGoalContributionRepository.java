package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaGoalContributionRepository extends JpaRepository<GoalContributionEntity, UUID> {
    List<GoalContributionEntity> findByGoalIdAndActiveTrueOrderByDateDesc(UUID goalId);
}
