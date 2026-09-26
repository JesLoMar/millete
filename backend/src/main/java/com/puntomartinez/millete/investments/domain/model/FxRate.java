package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FxRate(UUID id, String baseCurrency, String quoteCurrency,
                     Instant timestamp, BigDecimal rate, String source,
                     Instant fetchedAt) {
    public FxRate {
        if (id == null || timestamp == null || fetchedAt == null) throw new IllegalArgumentException("FX identifiers and timestamps are required");
        if (baseCurrency == null || quoteCurrency == null || baseCurrency.equalsIgnoreCase(quoteCurrency)) throw new IllegalArgumentException("FX currencies must be distinct");
        if (rate == null || rate.signum() <= 0) throw new IllegalArgumentException("FX rate must be positive");
        if (source == null || source.isBlank()) throw new IllegalArgumentException("FX source is required");
        baseCurrency = baseCurrency.trim().toUpperCase();
        quoteCurrency = quoteCurrency.trim().toUpperCase();
        source = source.trim();
    }
}
