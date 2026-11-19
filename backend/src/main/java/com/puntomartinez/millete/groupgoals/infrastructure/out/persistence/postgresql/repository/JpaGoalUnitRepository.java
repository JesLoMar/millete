package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalUnitEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaGoalUnitRepository
        extends JpaRepository<GoalUnitEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT gu FROM GoalUnitEntity gu WHERE gu.id = :id")
    Optional<GoalUnitEntity> findByIdForUpdate(@Param("id") UUID id);

    @Query(value = """
            SELECT gu
            FROM GoalUnitEntity gu
            JOIN GoalMemberEntity gm
              ON gm.goalId = gu.id
             AND gm.userId = :userId
             AND gm.active = true
            WHERE gu.active = true
            ORDER BY
                CASE WHEN gm.role = 'ADMIN' THEN 0 ELSE 1 END,
                LOWER(gu.name)
            """,
            countQuery = """
                    SELECT COUNT(gu)
                    FROM GoalUnitEntity gu
                    JOIN GoalMemberEntity gm
                      ON gm.goalId = gu.id
                     AND gm.userId = :userId
                     AND gm.active = true
                    WHERE gu.active = true
                    """)
    Page<GoalUnitEntity> findActiveByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(gu)
            FROM GoalUnitEntity gu
            WHERE gu.active = true
              AND gu.id IN (
                  SELECT gm.goalId
                  FROM GoalMemberEntity gm
                  WHERE gm.userId = :userId
                    AND gm.active = true
              )
            """)
    long countActiveByUserId(
            UUID userId
    );
}
