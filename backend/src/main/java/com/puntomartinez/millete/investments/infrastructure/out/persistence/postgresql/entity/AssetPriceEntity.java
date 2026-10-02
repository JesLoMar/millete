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
@Table(name = "asset_prices")
@Getter
@Setter
@NoArgsConstructor
public class AssetPriceEntity {

    @Id
    private UUID id;

    @Column(name = "shared_asset_id", nullable = false)
    private UUID sharedAssetId;

    @Column(name = "price_timestamp", nullable = false)
    private Instant timestamp;

    @Column(precision = 28, scale = 12)
    private BigDecimal open;

    @Column(precision = 28, scale = 12)
    private BigDecimal high;

    @Column(precision = 28, scale = 12)
    private BigDecimal low;

    @Column(precision = 28, scale = 12)
    private BigDecimal close;

    @Column(name = "adjusted_close", precision = 28, scale = 12)
    private BigDecimal adjustedClose;

    @Column(precision = 32, scale = 12)
    private BigDecimal volume;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}