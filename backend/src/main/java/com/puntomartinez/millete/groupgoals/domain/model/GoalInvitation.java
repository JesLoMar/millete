package com.puntomartinez.millete.groupgoals.domain.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class GoalInvitation {
    private UUID id;
    private UUID goalId;
    private String email;
    private String token;
    private UUID inviterUserId;
    private UUID invitedUserId;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    public GoalInvitation() {
        this.id = UUID.randomUUID();
        this.status = InvitationStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusDays(7);
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.active = true;
    }

    public boolean isAcceptable() {
        return InvitationStatus.PENDING.equals(this.status)
                && this.expiresAt.isAfter(LocalDateTime.now())
                && this.active;
    }

    public void markAsAccepted() {
        this.status = InvitationStatus.ACCEPTED;
        this.modifiedAt = LocalDateTime.now();
    }

    public void markAsRejected() {
        this.status = InvitationStatus.REJECTED;
        this.modifiedAt = LocalDateTime.now();
    }
}