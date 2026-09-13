package com.puntomartinez.millete.groupgoals.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class GoalInvitation {

    private final UUID id;
    private final UUID goalId;
    private final String email;
    private final String token;
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
            String email,
            String token,
            UUID inviterUserId,
            UUID invitedUserId,
            InvitationStatus status,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active) {

        this.id = requireId(id);
        this.goalId = requireGoalId(goalId);
        this.email = email;
        this.token = requireToken(token);
        this.inviterUserId = requireInviterUserId(inviterUserId);
        this.invitedUserId = requireInvitedUserId(invitedUserId);
        this.status = requireStatus(status);
        this.expiresAt = requireDate(
                expiresAt,
                "La fecha de expiración es obligatoria."
        );
        this.createdAt = requireDate(
                createdAt,
                "La fecha de creación es obligatoria."
        );
        this.modifiedAt = requireDate(
                modifiedAt,
                "La fecha de modificación es obligatoria."
        );
        this.active = active;
    }

    public static GoalInvitation create(
            UUID goalId,
            String email,
            UUID inviterUserId,
            UUID invitedUserId) {

        LocalDateTime now = LocalDateTime.now();

        return new GoalInvitation(
                UUID.randomUUID(),
                goalId,
                email,
                UUID.randomUUID().toString(),
                inviterUserId,
                invitedUserId,
                InvitationStatus.PENDING,
                now.plusDays(7),
                now,
                now,
                true
        );
    }

    public static GoalInvitation reconstitute(
            UUID id,
            UUID goalId,
            String email,
            String token,
            UUID inviterUserId,
            UUID invitedUserId,
            InvitationStatus status,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active) {

        return new GoalInvitation(
                id,
                goalId,
                email,
                token,
                inviterUserId,
                invitedUserId,
                status,
                expiresAt,
                createdAt,
                modifiedAt,
                active
        );
    }

    public boolean isAcceptable() {
        return InvitationStatus.PENDING.equals(status)
                && expiresAt.isAfter(LocalDateTime.now())
                && active;
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
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El id de la invitación es obligatorio."
            );
        }
        return id;
    }

    private static UUID requireGoalId(UUID goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException(
                    "La meta es obligatoria."
            );
        }
        return goalId;
    }

    private static UUID requireInviterUserId(UUID inviterUserId) {
        if (inviterUserId == null) {
            throw new IllegalArgumentException(
                    "El usuario que invita es obligatorio."
            );
        }
        return inviterUserId;
    }

    private static UUID requireInvitedUserId(UUID invitedUserId) {
        if (invitedUserId == null) {
            throw new IllegalArgumentException(
                    "El usuario invitado es obligatorio."
            );
        }
        return invitedUserId;
    }

    private static String requireToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "El token de invitación es obligatorio."
            );
        }
        return token;
    }

    private static InvitationStatus requireStatus(
            InvitationStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "El estado de la invitación es obligatorio."
            );
        }
        return status;
    }

    private static LocalDateTime requireDate(
            LocalDateTime value,
            String message) {

        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public UUID getInviterUserId() {
        return inviterUserId;
    }

    public UUID getInvitedUserId() {
        return invitedUserId;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public boolean isActive() {
        return active;
    }
}