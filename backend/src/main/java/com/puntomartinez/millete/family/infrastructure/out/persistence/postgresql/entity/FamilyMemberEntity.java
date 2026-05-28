package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "family_members")
@Data
public class FamilyMemberEntity {
    @Id
    private UUID id;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String role;

    private BigDecimal salary;

    @Column(name = "custom_percentage")
    private BigDecimal customPercentage;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(nullable = false)
    private boolean active;
}