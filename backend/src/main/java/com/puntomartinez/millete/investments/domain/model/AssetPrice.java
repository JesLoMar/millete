package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssetPrice(UUID id, UUID userId, UUID assetId, Instant timestamp,
                         BigDecimal open, BigDecimal high, BigDecimal low,
                         BigDecimal close, BigDecimal adjustedClose,
                         BigDecimal volume, String currency, String source,
                         Instant fetchedAt) {
    public AssetPrice {
        if (id == null || userId == null || assetId == null || timestamp == null || fetchedAt == null) throw new IllegalArgumentException("Price identifiers and timestamps are required");
        if (currency == null || currency.isBlank() || source == null || source.isBlank()) throw new IllegalArgumentException("Price currency and source are required");
        for (BigDecimal value : new BigDecimal[]{open, high, low, close, adjustedClose, volume}) {
            if (value != null && value.signum() < 0) throw new IllegalArgumentException("Price and volume values cannot be negative");
        }
        currency = currency.trim().toUpperCase();
        source = source.trim();
    }
}
