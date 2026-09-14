package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface JpaGoalContributionRepository
        extends JpaRepository<GoalContributionEntity, UUID> {

    List<GoalContributionEntity> findByGoalIdAndActiveTrueOrderByDateDesc(
            UUID goalId
    );

    Page<GoalContributionEntity> findByGoalIdAndActiveTrueOrderByDateDesc(
            UUID goalId,
            Pageable pageable
    );

    long countByGoalIdAndActiveTrue(UUID goalId);

    @Query("""
            SELECT c.userId, SUM(c.amount)
            FROM GoalContributionEntity c
            WHERE c.goalId = :goalId
              AND c.active = true
            GROUP BY c.userId
            """)
    List<Object[]> sumByUserId(
            @Param("goalId") UUID goalId
    );
}