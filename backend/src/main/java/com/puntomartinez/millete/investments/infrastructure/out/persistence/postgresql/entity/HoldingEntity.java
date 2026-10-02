package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "holdings")
@Getter
@Setter
@NoArgsConstructor
public class HoldingEntity {

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
            name = "asset_reference_kind",
            nullable = false,
            updatable = false,
            length = 10
    )
    private String assetReferenceKind;

    @Column(
            name = "asset_reference_id",
            nullable = false,
            updatable = false
    )
    private UUID assetReferenceId;

    @Column(
            name = "snapshot_at",
            nullable = false,
            updatable = false
    )
    private Instant snapshotAt;

    @Column(
            nullable = false,
            precision = 28,
            scale = 12,
            updatable = false
    )
    private BigDecimal quantity;

    @Column(
            name = "acquisition_cost",
            nullable = false,
            precision = 28,
            scale = 12,
            updatable = false
    )
    private BigDecimal acquisitionCost;

    @Column(
            name = "acquisition_cost_currency",
            nullable = false,
            length = 3,
            updatable = false
    )
    private String acquisitionCostCurrency;

    @Column(
            nullable = false,
            length = 20
    )
    private String status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "superseded_at")
    private Instant supersededAt;
}