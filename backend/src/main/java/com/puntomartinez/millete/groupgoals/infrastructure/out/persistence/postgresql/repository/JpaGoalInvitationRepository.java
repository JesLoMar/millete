package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
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
public interface JpaGoalInvitationRepository
        extends JpaRepository<GoalInvitationEntity, UUID> {

    Optional<GoalInvitationEntity> findByIdAndActiveTrue(UUID id);

    Optional<GoalInvitationEntity> findByGoalIdAndInvitedUserIdAndStatusAndActiveTrue(
            UUID goalId,
            UUID invitedUserId,
            String status
    );

    @Query("""
            SELECT i
            FROM GoalInvitationEntity i
            WHERE i.invitedUserId = :invitedUserId
              AND i.status = :status
              AND i.active = true
              AND i.expiresAt > :now
            """)
    List<GoalInvitationEntity> findActiveAndNotExpiredByInvitedUserIdAndStatus(
            @Param("invitedUserId") UUID invitedUserId,
            @Param("status") String status,
            @Param("now") LocalDateTime now
    );

    List<GoalInvitationEntity> findByGoalIdAndStatusAndActiveTrue(
            UUID goalId,
            String status
    );

    @Modifying
    @Query("""
            UPDATE GoalInvitationEntity i
            SET i.active = false,
                i.modifiedAt = :modifiedAt
            WHERE i.goalId = :goalId
              AND i.status = 'PENDING'
              AND i.active = true
            """)
    int deactivatePendingByGoalId(
            @Param("goalId") UUID goalId,
            @Param("modifiedAt") LocalDateTime modifiedAt
    );
}