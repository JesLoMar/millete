package com.puntomartinez.millete.investments.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
public class Investment {

    public enum InvestmentType {
        STOCK, CRYPTO, FUND, REAL_ESTATE, OTHER
    }

    private UUID id;
    private UUID userId;
    private String assetName;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private BigDecimal currentPrice;
    private InvestmentType type;
    private LocalDateTime purchaseDate;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    public Investment(UUID id, UUID userId, String assetName, String ticker,
                      BigDecimal quantity, BigDecimal purchasePrice, BigDecimal currentPrice,
                      InvestmentType type, LocalDateTime purchaseDate,
                      LocalDateTime createdAt, LocalDateTime modifiedAt, boolean active) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        this.id = id;
        this.userId = userId;
        this.assetName = assetName;
        this.ticker = ticker;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice != null ? currentPrice : purchasePrice;
        this.type = type;
        this.purchaseDate = purchaseDate;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    // =======================================================
    // MÉTODOS DE NEGOCIO
    // =======================================================

    public BigDecimal getInvestedCapital() {
        return quantity.multiply(purchasePrice);
    }

    public BigDecimal getCurrentValue() {
        return quantity.multiply(currentPrice);
    }

    public BigDecimal getProfitOrLoss() {
        return getCurrentValue().subtract(getInvestedCapital());
    }

    public BigDecimal getReturnOnInvestmentPercentage() {
        BigDecimal invested = getInvestedCapital();
        if (invested.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal profit = getProfitOrLoss();
        return profit.divide(invested, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    // =======================================================
    // MÉTODOS DE ACTUALIZACIÓN DE ESTADO
    // =======================================================

    public void updateCurrentPrice(BigDecimal newPrice) {
        if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) >= 0) {
            this.currentPrice = newPrice;
            this.modifiedAt = LocalDateTime.now();
        }
    }

    public void deactivate() {
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }
}