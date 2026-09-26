package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Lot {
    private final UUID id;
    private final UUID userId;
    private final UUID assetId;
    private final UUID sourceActivityId;
    private final UUID sourceHoldingId;
    private final Instant acquiredAt;
    private BigDecimal originalQuantity;
    private BigDecimal remainingQuantity;
    private BigDecimal totalCost;
    private final String currency;
    private final boolean synthetic;

    public Lot(UUID id, UUID userId, UUID assetId, UUID sourceActivityId, UUID sourceHoldingId,
               Instant acquiredAt, BigDecimal originalQuantity,
               BigDecimal remainingQuantity, BigDecimal totalCost,
               String currency, boolean synthetic) {
        if (id == null || userId == null || assetId == null || acquiredAt == null) throw new IllegalArgumentException("Lot identity and acquiredAt are required");
        positive(originalQuantity, "originalQuantity");
        if (remainingQuantity == null || remainingQuantity.signum() < 0 || remainingQuantity.compareTo(originalQuantity) > 0) throw new IllegalArgumentException("remainingQuantity is outside lot bounds");
        if (totalCost == null || totalCost.signum() < 0) throw new IllegalArgumentException("totalCost cannot be negative");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency is required");
        this.id = id; this.userId = userId; this.assetId = assetId;
        this.sourceActivityId = sourceActivityId; this.acquiredAt = acquiredAt;
        this.sourceHoldingId = sourceHoldingId;
        this.originalQuantity = originalQuantity; this.remainingQuantity = remainingQuantity;
        this.totalCost = totalCost; this.currency = currency.trim().toUpperCase();
        this.synthetic = synthetic;
    }

    public BigDecimal consume(BigDecimal units) {
        positive(units, "consumed quantity");
        if (units.compareTo(remainingQuantity) > 0) throw new IllegalArgumentException("Lot does not contain enough units");
        BigDecimal unitCost = getUnitCost();
        remainingQuantity = remainingQuantity.subtract(units);
        return unitCost.multiply(units);
    }

    public void split(BigDecimal ratio) {
        positive(ratio, "split ratio");
        originalQuantity = originalQuantity.multiply(ratio);
        remainingQuantity = remainingQuantity.multiply(ratio);
    }

    public BigDecimal getUnitCost() {
        return totalCost.divide(originalQuantity, 16, java.math.RoundingMode.HALF_UP);
    }

    private static void positive(BigDecimal n, String name) { if (n == null || n.signum() <= 0) throw new IllegalArgumentException(name + " must be positive"); }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getAssetId() { return assetId; }
    public UUID getSourceActivityId() { return sourceActivityId; }
    public UUID getSourceHoldingId() { return sourceHoldingId; }
    public Instant getAcquiredAt() { return acquiredAt; }
    public BigDecimal getOriginalQuantity() { return originalQuantity; }
    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public BigDecimal getTotalCost() { return totalCost; }
    public String getCurrency() { return currency; }
    public boolean isSynthetic() { return synthetic; }
}
