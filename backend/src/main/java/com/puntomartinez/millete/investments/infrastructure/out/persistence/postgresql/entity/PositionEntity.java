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
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor
public class PositionEntity {

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
            nullable = false,
            precision = 28,
            scale = 12
    )
    private BigDecimal quantity;

    @Column(
            name = "acquisition_cost",
            nullable = false,
            precision = 28,
            scale = 12
    )
    private BigDecimal acquisitionCost;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "history_incomplete",
            nullable = false
    )
    private boolean historyIncomplete;

    @Column(
            nullable = false
    )
    private boolean estimated;
}