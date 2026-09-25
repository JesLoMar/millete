package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
@SQLRestriction("active = true")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    @Column(name = "budget_limit", precision = 12, scale = 2)
    private BigDecimal budgetLimit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    @Column(nullable = false)
    private boolean active;
}