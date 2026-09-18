package com.puntomartinez.millete.groupgoals.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.time.LocalDateTime;
import java.util.UUID;

public class GoalInvitation {

    private static final int DEFAULT_EXPIRATION_DAYS = 7;

    private final UUID id;
    private final UUID goalId;
    private final UUID inviterUserId;
    private final UUID invitedUserId;
    private InvitationStatus status;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    private GoalInvitation(
            UUID id,
            UUID goalId,
            UUID inviterUserId,
            UUID invitedUserId,
            InvitationStatus status,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active
    ) {
        validateId(id);
        validateGoalId(goalId);
        validateInviterUserId(inviterUserId);
        validateInvitedUserId(invitedUserId);
        validateStatus(status);
        validateExpiresAt(expiresAt);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        this.id = id;
        this.goalId = goalId;
        this.inviterUserId = inviterUserId;
        this.invitedUserId = invitedUserId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    public static GoalInvitation create(
            UUID goalId,
            UUID inviterUserId,
            UUID invitedUserId
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new GoalInvitation(
                UUID.randomUUID(),
                goalId,
                inviterUserId,
                invitedUserId,
                InvitationStatus.PENDING,
                now.plusDays(DEFAULT_EXPIRATION_DAYS),
                now,
                now,
                true
        );
    }

    public static GoalInvitation reconstitute(
            UUID id,
            UUID goalId,
            UUID inviterUserId,
            UUID invitedUserId,
            InvitationStatus status,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active
    ) {
        return new GoalInvitation(
                id, goalId, inviterUserId, invitedUserId,
                status, expiresAt, createdAt, modifiedAt, active
        );
    }

    public boolean isAcceptable() {
        return this.status == InvitationStatus.PENDING
                && this.active
                && LocalDateTime.now().isBefore(this.expiresAt);
    }

    public void markAsAccepted() {
        this.status = InvitationStatus.ACCEPTED;
        this.modifiedAt = LocalDateTime.now();
    }

    public void markAsRejected() {
        this.status = InvitationStatus.REJECTED;
        this.modifiedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new InvalidInputException("El id de la invitación es obligatorio");
        }
    }

    private static void validateGoalId(UUID goalId) {
        if (goalId == null) {
            throw new InvalidInputException("El objetivo de la invitación es obligatorio");
        }
    }

    private static void validateInviterUserId(UUID inviterUserId) {
        if (inviterUserId == null) {
            throw new InvalidInputException("El usuario que invita es obligatorio");
        }
    }

    private static void validateInvitedUserId(UUID invitedUserId) {
        if (invitedUserId == null) {
            throw new InvalidInputException("El usuario invitado es obligatorio");
        }
    }

    private static void validateStatus(InvitationStatus status) {
        if (status == null) {
            throw new InvalidInputException("El estado de la invitación es obligatorio");
        }
    }

    private static void validateExpiresAt(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new InvalidInputException("La fecha de expiración es obligatoria");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new InvalidInputException("La fecha de creación es obligatoria");
        }
    }

    private static void validateModifiedAt(LocalDateTime modifiedAt) {
        if (modifiedAt == null) {
            throw new InvalidInputException("La fecha de modificación es obligatoria");
        }
    }

    public UUID getId() { return id; }
    public UUID getGoalId() { return goalId; }
    public UUID getInviterUserId() { return inviterUserId; }
    public UUID getInvitedUserId() { return invitedUserId; }
    public InvitationStatus getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public boolean isActive() { return active; }
}