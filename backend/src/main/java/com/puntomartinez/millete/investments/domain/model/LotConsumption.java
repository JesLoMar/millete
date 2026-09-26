package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record LotConsumption(UUID id, UUID sellActivityId, UUID lotId,
                             BigDecimal quantity, BigDecimal costBasis,
                             String currency) {
    public LotConsumption {
        if (id == null || sellActivityId == null || lotId == null) throw new IllegalArgumentException("Consumption identifiers are required");
        if (quantity == null || quantity.signum() <= 0 || costBasis == null || costBasis.signum() < 0) throw new IllegalArgumentException("Consumption quantity and cost basis are invalid");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Consumption currency is required");
    }
}
