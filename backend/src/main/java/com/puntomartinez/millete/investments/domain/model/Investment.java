package com.puntomartinez.millete.investments.domain.model;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Investment {

    public enum InvestmentType {
        STOCK, CRYPTO, FUND, REAL_ESTATE, OTHER
    }

    private final UUID id;
    private final UUID userId;
    private String assetName;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private BigDecimal currentPrice;
    private InvestmentType type;
    private LocalDate purchaseDate;
    private final Instant createdAt;
    private Instant modifiedAt;
    private boolean active;

    private Investment(
            UUID id,
            UUID userId,
            String assetName,
            String ticker,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            BigDecimal currentPrice,
            InvestmentType type,
            LocalDate purchaseDate,
            Instant createdAt,
            Instant modifiedAt,
            boolean active) {

        this.id = requireId(id);
        this.userId = requireUserId(userId);
        this.assetName = requireAssetName(assetName);
        this.ticker = normalizeTicker(ticker, type);
        this.quantity = requirePositive(quantity, "La cantidad debe ser mayor que cero.");
        this.purchasePrice = requirePositive(
                purchasePrice,
                "El precio de compra debe ser mayor que cero."
        );
        this.currentPrice = requireNonNegative(
                currentPrice,
                "El precio actual no puede ser negativo."
        );
        this.type = requireType(type);
        this.purchaseDate = requirePurchaseDate(purchaseDate);
        this.createdAt = requireDate(createdAt, "La fecha de creación es obligatoria.");
        this.modifiedAt = requireDate(modifiedAt, "La fecha de modificación es obligatoria.");
        this.active = active;
    }

    public static Investment create(
            TimeProvider timeProvider,
            UUID userId,
            String assetName,
            String ticker,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            InvestmentType type,
            LocalDate purchaseDate) {

        Instant now = timeProvider.instantNow();

        return new Investment(
                UUID.randomUUID(),
                userId,
                assetName,
                ticker,
                quantity,
                purchasePrice,
                purchasePrice,
                type,
                purchaseDate,
                now,
                now,
                true
        );
    }

    public static Investment reconstitute(
            UUID id,
            UUID userId,
            String assetName,
            String ticker,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            BigDecimal currentPrice,
            InvestmentType type,
            LocalDate purchaseDate,
            Instant createdAt,
            Instant modifiedAt,
            boolean active) {

        return new Investment(
                id,
                userId,
                assetName,
                ticker,
                quantity,
                purchasePrice,
                currentPrice,
                type,
                purchaseDate,
                createdAt,
                modifiedAt,
                active
        );
    }

    public void updateDetails(TimeProvider timeProvider, 
        String assetName,
        String ticker,
        BigDecimal quantity,
        BigDecimal purchasePrice,
        InvestmentType type,
        LocalDate purchaseDate) {

    this.assetName = requireAssetName(assetName);
    this.ticker = normalizeTicker(ticker, type);
    this.quantity = requirePositive(
            quantity,
            "La cantidad debe ser mayor que cero."
    );
    this.purchasePrice = requirePositive(
            purchasePrice,
            "El precio de compra debe ser mayor que cero."
    );
    this.type = requireType(type);
    this.purchaseDate = requirePurchaseDate(purchaseDate);
    this.modifiedAt = timeProvider.instantNow();
}

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

        if (invested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profit = getProfitOrLoss();

        return profit
                .divide(invested, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public void updateCurrentPrice(TimeProvider timeProvider, BigDecimal newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("El precio actual es obligatorio.");
        }

        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El precio actual no puede ser negativo."
            );
        }

        this.currentPrice = newPrice;
        this.modifiedAt = timeProvider.instantNow();
    }

    public void deactivate(TimeProvider timeProvider) {
        this.active = false;
        this.modifiedAt = timeProvider.instantNow();
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la inversión es obligatorio.");
        }

        return id;
    }

    private static UUID requireUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }

        return userId;
    }

    private static String requireAssetName(String assetName) {
        if (assetName == null || assetName.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del activo es obligatorio."
            );
        }

        return assetName.trim();
    }

    private static InvestmentType requireType(InvestmentType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de inversión es obligatorio."
            );
        }

        return type;
    }

    private static LocalDate requirePurchaseDate(LocalDate purchaseDate) {
        if (purchaseDate == null) {
            throw new IllegalArgumentException(
                    "La fecha de compra es obligatoria."
            );
        }

        return purchaseDate;
    }

    private static Instant requireDate(
            Instant value,
            String message) {

        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    private static BigDecimal requirePositive(
            BigDecimal value,
            String message) {

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    private static BigDecimal requireNonNegative(
            BigDecimal value,
            String message) {

        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    private static String normalizeTicker(
            String ticker,
            InvestmentType type) {

        String normalizedTicker =
                ticker == null || ticker.isBlank()
                        ? null
                        : ticker.trim().toUpperCase();

        if (requiresTicker(type)
                && (normalizedTicker == null || normalizedTicker.isBlank())) {

            throw new IllegalArgumentException(
                    "El ticker es obligatorio para inversiones de tipo "
                            + type + "."
            );
        }

        return normalizedTicker;
    }

    private static boolean requiresTicker(InvestmentType type) {
        return type == InvestmentType.STOCK
                || type == InvestmentType.CRYPTO
                || type == InvestmentType.FUND;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public InvestmentType getType() {
        return type;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public boolean isActive() {
        return active;
    }
}