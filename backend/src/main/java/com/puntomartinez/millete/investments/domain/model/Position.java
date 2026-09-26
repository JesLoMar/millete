package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Position(UUID userId, UUID assetId, BigDecimal quantity,
                       BigDecimal acquisitionCost, String currency,
                       boolean open, boolean historyIncomplete) {
    public Position {
        if (userId == null || assetId == null || quantity == null || acquisitionCost == null || currency == null) {
            throw new IllegalArgumentException("Position values are required");
        }
        if (quantity.signum() < 0 || acquisitionCost.signum() < 0) throw new IllegalArgumentException("Position values cannot be negative");
        if (open != (quantity.signum() > 0)) throw new IllegalArgumentException("Position open state must match its quantity");
    }
}
