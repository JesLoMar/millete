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
@Table(name = "lots")
@Getter
@Setter
@NoArgsConstructor
public class LotEntity {

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
            name = "source_type",
            nullable = false,
            updatable = false,
            length = 20
    )
    private String sourceType;

    @Column(
            name = "source_id",
            nullable = false,
            updatable = false
    )
    private UUID sourceId;

    @Column(
            name = "acquired_at",
            nullable = false,
            updatable = false
    )
    private Instant acquiredAt;

    @Column(
            name = "acquisition_order",
            nullable = false,
            updatable = false
    )
    private long acquisitionOrder;

    @Column(
            name = "original_quantity",
            nullable = false,
            precision = 28,
            scale = 12
    )
    private BigDecimal originalQuantity;

    @Column(
            name = "remaining_quantity",
            nullable = false,
            precision = 28,
            scale = 12
    )
    private BigDecimal remainingQuantity;

    @Column(
            name = "total_cost",
            nullable = false,
            precision = 28,
            scale = 12,
            updatable = false
    )
    private BigDecimal totalCost;

    @Column(
            nullable = false,
            length = 3,
            updatable = false
    )
    private String currency;
}