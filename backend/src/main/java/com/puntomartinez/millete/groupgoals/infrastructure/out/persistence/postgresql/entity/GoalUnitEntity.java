package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "goal_units")
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

    @Column(name = "monthly_target", precision = 12, scale = 2)
    private BigDecimal monthlyTarget;

    @Column(name = "distribution_mode", nullable = false, length = 20)
    private String distributionMode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(nullable = false)
    private boolean active;
}
