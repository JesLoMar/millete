package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "family_invitations")
@Data
public class FamilyInvitationEntity {
    @Id
    private UUID id;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(nullable = false)
    private boolean active;
}