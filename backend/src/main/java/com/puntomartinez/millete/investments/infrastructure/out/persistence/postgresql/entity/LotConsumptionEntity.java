package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "lot_consumptions")
@Getter
@Setter
@NoArgsConstructor
public class LotConsumptionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private UUID userId;

    @Column(
            name = "sell_activity_id",
            nullable = false,
            updatable = false
    )
    private UUID sellActivityId;

    @Column(
            name = "lot_id",
            nullable = false,
            updatable = false
    )
    private UUID lotId;

    @Column(
            nullable = false,
            precision = 28,
            scale = 12,
            updatable = false
    )
    private BigDecimal quantity;

    @Column(
            name = "cost_basis",
            nullable = false,
            precision = 28,
            scale = 12,
            updatable = false
    )
    private BigDecimal costBasis;

    @Column(
            nullable = false,
            length = 3,
            updatable = false
    )
    private String currency;
}