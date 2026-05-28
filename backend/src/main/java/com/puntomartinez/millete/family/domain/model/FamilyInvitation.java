package com.puntomartinez.millete.family.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class FamilyInvitation {
    private UUID id;
    private UUID familyId;
    private String email;
    private String token;
    private InvitationStatus status;
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    /**
     * BUSINESS LOGIC - STORY 8
     */
    public boolean isAcceptable() {
        return InvitationStatus.PENDING.equals(this.status)
                && this.expiresAt.isAfter(LocalDateTime.now())
                && this.active;
    }

    public void markAsAccepted() {
        this.status = InvitationStatus.ACCEPTED;
        this.modifiedAt = LocalDateTime.now();
    }
}