package com.puntomartinez.millete.investments.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class SharedAsset {

    private final UUID id;
    private final String stableCatalogId;
    private String name;
    private String symbol;
    private final AssetType type;
    private AssetSector sector;
    private final CurrencyCode currency;
    private final Instant createdAt;
    private Instant modifiedAt;

    private SharedAsset(
            UUID id,
            String stableCatalogId,
            String name,
            String symbol,
            AssetType type,
            AssetSector sector,
            CurrencyCode currency,
            Instant createdAt,
            Instant modifiedAt
    ) {
        validateId(id);
        validateStableCatalogId(stableCatalogId);
        validateName(name);
        validateSymbol(symbol);
        validateType(type);
        validateSector(sector);
        validateCurrency(currency);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        this.id = id;
        this.stableCatalogId = stableCatalogId;
        this.name = name;
        this.symbol = normalizeSymbol(symbol);
        this.type = type;
        this.sector = sector;
        this.currency = currency;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static SharedAsset create(
            TimeProvider timeProvider,
            String stableCatalogId,
            String name,
            String symbol,
            AssetType type,
            AssetSector sector,
            CurrencyCode currency
    ) {
        Instant now = timeProvider.now();

        return new SharedAsset(
                UUID.randomUUID(),
                stableCatalogId,
                name,
                symbol,
                type,
                sector,
                currency,
                now,
                now
        );
    }

    public static SharedAsset reconstitute(
            UUID id,
            String stableCatalogId,
            String name,
            String symbol,
            AssetType type,
            AssetSector sector,
            CurrencyCode currency,
            Instant createdAt,
            Instant modifiedAt
    ) {
        return new SharedAsset(
                id,
                stableCatalogId,
                name,
                symbol,
                type,
                sector,
                currency,
                createdAt,
                modifiedAt
        );
    }

    public void updateDetails(
            TimeProvider timeProvider,
            String name,
            String symbol,
            AssetSector sector
    ) {
        validateName(name);
        validateSymbol(symbol);
        validateSector(sector);

        this.name = name;
        this.symbol = normalizeSymbol(symbol);
        this.sector = sector;
        this.modifiedAt = timeProvider.now();
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del SharedAsset es obligatorio"
            );
        }
    }

    private static void validateStableCatalogId(
            String stableCatalogId
    ) {
        if (stableCatalogId == null
                || stableCatalogId.isBlank()) {

            throw new IllegalArgumentException(
                    "El identificador estable del catálogo es obligatorio"
            );
        }
    }

    private static void validateName(String name) {
        if (name == null
                || name.isBlank()
                || name.trim().length() > 120) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio y no puede superar los 120 caracteres"
            );
        }
    }

    private static void validateSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "El símbolo del SharedAsset es obligatorio"
            );
        }
    }

    private static void validateType(AssetType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de Asset es obligatorio"
            );
        }

        if (type != AssetType.STOCK
                && type != AssetType.ETF
                && type != AssetType.CRYPTO) {

            throw new IllegalArgumentException(
                    "Un SharedAsset solo puede ser de tipo STOCK, ETF o CRYPTO"
            );
        }
    }

    private static void validateSector(AssetSector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                    "El sector es obligatorio"
            );
        }

        if (sector.custom()) {
            throw new IllegalArgumentException(
                    "Un SharedAsset debe utilizar un sector del catálogo"
            );
        }
    }

    private static void validateCurrency(
            CurrencyCode currency
    ) {
        if (currency == null) {
            throw new IllegalArgumentException(
                    "La moneda es obligatoria"
            );
        }
    }

    private static void validateCreatedAt(
            Instant createdAt
    ) {
        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria"
            );
        }
    }

    private static void validateModifiedAt(
            Instant modifiedAt
    ) {
        if (modifiedAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de modificación es obligatoria"
            );
        }
    }

    private static String normalizeSymbol(
            String symbol
    ) {
        return symbol.trim().toUpperCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public String getStableCatalogId() {
        return stableCatalogId;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public AssetType getType() {
        return type;
    }

    public AssetSector getSector() {
        return sector;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }
}