package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Holding(UUID id, UUID userId, UUID assetId, Instant snapshotAt,
                      BigDecimal quantity, BigDecimal acquisitionCost,
                      String currency, boolean historyIncomplete,
                      boolean superseded, Instant createdAt) {
    public Holding {
        if (id == null || userId == null || assetId == null || snapshotAt == null || createdAt == null) {
            throw new IllegalArgumentException("Holding identifiers and timestamps are required");
        }
        if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("Holding quantity must be positive");
        if (acquisitionCost == null || acquisitionCost.signum() < 0) throw new IllegalArgumentException("Acquisition cost cannot be negative");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Holding currency is required");
        currency = currency.trim().toUpperCase();
    }
}
