package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "family_units")
@Data
public class FamilyUnitEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "monthly_target")
    private BigDecimal monthlyTarget;

    @Column(name = "distribution_mode", nullable = false)
    private String distributionMode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(nullable = false)
    private boolean active;
}