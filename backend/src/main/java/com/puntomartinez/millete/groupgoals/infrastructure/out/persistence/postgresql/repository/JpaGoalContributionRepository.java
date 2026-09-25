package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.projection.ContributionTotalsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaGoalContributionRepository
        extends JpaRepository<GoalContributionEntity, UUID> {

    Page<GoalContributionEntity> findByGoalIdAndActiveTrueOrderByDateDesc(
            UUID goalId,
            Pageable pageable
    );

    long countByGoalIdAndActiveTrue(UUID goalId);

    @Query("""
            SELECT
                c.userId AS userId,
                COALESCE(SUM(CASE WHEN c.type = 'DEPOSIT' THEN c.amount ELSE 0 END), 0) AS totalDeposits,
                COALESCE(SUM(CASE WHEN c.type = 'WITHDRAWAL' THEN c.amount ELSE 0 END), 0) AS totalWithdrawals
            FROM GoalContributionEntity c
            WHERE c.goalId = :goalId
              AND c.active = true
            GROUP BY c.userId
            """)
    List<ContributionTotalsProjection> sumByGoalIdGroupedByUser(
            @Param("goalId") UUID goalId
    );

    @Modifying
    @Query("""
            UPDATE GoalContributionEntity c
            SET c.active = false,
                c.modifiedAt = :modifiedAt
            WHERE c.goalId = :goalId
              AND c.active = true
            """)
    int deactivateByGoalId(
            @Param("goalId") UUID goalId,
            @Param("modifiedAt") Instant modifiedAt
    );
}