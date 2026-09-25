package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "goal_units")
@SQLRestriction("active = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalUnitEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(
        name = "monthly_target",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal monthlyTarget;

    @Column(name = "distribution_mode", nullable = false, length = 20)
    private String distributionMode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    @Column(nullable = false)
    private boolean active;
}