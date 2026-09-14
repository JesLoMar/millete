package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaGoalInvitationRepository
        extends JpaRepository<GoalInvitationEntity, UUID> {

    Optional<GoalInvitationEntity> findByToken(String token);

    Optional<GoalInvitationEntity> findByGoalIdAndEmailAndStatus(
            UUID goalId,
            String email,
            String status
    );

    List<GoalInvitationEntity> findByInvitedUserIdAndStatusAndActiveTrueAndExpiresAtAfter(
            UUID invitedUserId,
            String status,
            LocalDateTime now
    );

    Optional<GoalInvitationEntity> findByGoalIdAndInvitedUserIdAndStatus(
            UUID goalId,
            UUID invitedUserId,
            String status
    );
}