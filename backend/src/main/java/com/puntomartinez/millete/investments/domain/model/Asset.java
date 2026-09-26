package com.puntomartinez.millete.investments.domain.model;

import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

public final class Asset {
    private final UUID id;
    private final UUID userId;
    private String name;
    private String symbol;
    private AssetType type;
    private UUID sectorId;
    private String currency;
    private final Instant createdAt;
    private Instant modifiedAt;
    private boolean active;

    private Asset(UUID id, UUID userId, String name, String symbol,
                  AssetType type, UUID sectorId, String currency,
                  Instant createdAt, Instant modifiedAt, boolean active) {
        this.id = require(id, "id");
        this.userId = require(userId, "userId");
        this.name = requireText(name, "name", 120);
        this.symbol = normalizeSymbol(symbol);
        this.type = require(type, "type");
        this.sectorId = sectorId;
        this.currency = normalizeCurrency(currency);
        this.createdAt = require(createdAt, "createdAt");
        this.modifiedAt = require(modifiedAt, "modifiedAt");
        this.active = active;
    }

    public static Asset create(UUID userId, String name, String symbol,
                               AssetType type, UUID sectorId, String currency,
                               Instant now) {
        return new Asset(UUID.randomUUID(), userId, name, symbol, type,
                sectorId, currency, now, now, true);
    }

    public static Asset reconstitute(UUID id, UUID userId, String name,
                                     String symbol, AssetType type,
                                     UUID sectorId, String currency,
                                     Instant createdAt, Instant modifiedAt,
                                     boolean active) {
        return new Asset(id, userId, name, symbol, type, sectorId, currency,
                createdAt, modifiedAt, active);
    }

    public void update(String name, String symbol, AssetType type,
                       UUID sectorId, String currency, Instant now) {
        this.name = requireText(name, "name", 120);
        this.symbol = normalizeSymbol(symbol);
        this.type = require(type, "type");
        this.sectorId = sectorId;
        this.currency = normalizeCurrency(currency);
        this.modifiedAt = require(now, "now");
    }

    public void deactivate(Instant now) {
        this.active = false;
        this.modifiedAt = require(now, "now");
    }

    private static String normalizeCurrency(String value) {
        if (value == null || !value.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("Currency must be an ISO 4217 code");
        }
        String code = value.toUpperCase(Locale.ROOT);
        Currency.getInstance(code);
        return code;
    }

    private static String normalizeSymbol(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String requireText(String value, String name, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(name + " is required and must be at most " + max + " characters");
        }
        return value.trim();
    }

    private static <T> T require(T value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " is required");
        return value;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getSymbol() { return symbol; }
    public AssetType getType() { return type; }
    public UUID getSectorId() { return sectorId; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getModifiedAt() { return modifiedAt; }
    public boolean isActive() { return active; }
}
