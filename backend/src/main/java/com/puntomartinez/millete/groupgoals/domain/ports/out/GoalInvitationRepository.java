package com.puntomartinez.millete.groupgoals.domain.ports.out;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalInvitationRepository {

    GoalInvitation save(GoalInvitation invitation);

    Optional<GoalInvitation> findByToken(String token);

    Optional<GoalInvitation> findById(UUID id);

    Optional<GoalInvitation> findByGoalIdAndEmailAndStatus(
            UUID goalId,
            String email,
            InvitationStatus status
    );

    List<GoalInvitation> findActiveAndNotExpiredByInvitedUserIdAndStatus(
            UUID invitedUserId,
            InvitationStatus status,
            LocalDateTime now
    );

    Optional<GoalInvitation> findByGoalIdAndInvitedUserIdAndStatus(
            UUID goalId,
            UUID invitedUserId,
            InvitationStatus status
    );
}