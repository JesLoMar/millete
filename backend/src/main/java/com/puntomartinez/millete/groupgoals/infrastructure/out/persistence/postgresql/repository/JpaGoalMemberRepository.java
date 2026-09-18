package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaGoalMemberRepository
        extends JpaRepository<GoalMemberEntity, UUID> {

    Optional<GoalMemberEntity> findByIdAndActiveTrue(UUID id);

    Optional<GoalMemberEntity> findByGoalIdAndUserId(
            UUID goalId,
            UUID userId
    );

    List<GoalMemberEntity> findByGoalIdAndActiveTrue(UUID goalId);

    List<GoalMemberEntity> findByGoalIdInAndActiveTrue(List<UUID> goalIds);

    List<GoalMemberEntity> findByUserIdAndActiveTrue(UUID userId);

    @Modifying
    @Query("""
            UPDATE GoalMemberEntity m
            SET m.active = false,
                m.modifiedAt = :modifiedAt
            WHERE m.goalId = :goalId
              AND m.active = true
            """)
    int deactivateByGoalId(
            @Param("goalId") UUID goalId,
            @Param("modifiedAt") LocalDateTime modifiedAt
    );
}