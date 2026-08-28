package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalUnitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaGoalUnitRepository extends JpaRepository<GoalUnitEntity, UUID> {

    @Query(value = """
            SELECT gu FROM GoalUnitEntity gu
            WHERE gu.active = true
              AND gu.id IN (
                  SELECT gm.goalId FROM GoalMemberEntity gm
                  WHERE gm.userId = :userId AND gm.active = true
              )
            ORDER BY gu.name
            """,
            countQuery = """
                    SELECT count(gu) FROM GoalUnitEntity gu
                    WHERE gu.active = true
                      AND gu.id IN (
                          SELECT gm.goalId FROM GoalMemberEntity gm
                          WHERE gm.userId = :userId AND gm.active = true
                      )
                    """)
    Page<GoalUnitEntity> findActiveByUserId(@Param("userId") UUID userId, Pageable pageable);
}
