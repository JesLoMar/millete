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
@Table(name = "fx_rates")
@Getter
@Setter
@NoArgsConstructor
public class FxRateEntity {

    @Id
    private UUID id;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;

    @Column(name = "rate_timestamp", nullable = false)
    private Instant timestamp;

    @Column(nullable = false, precision = 28, scale = 16)
    private BigDecimal rate;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}