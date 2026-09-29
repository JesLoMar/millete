package com.puntomartinez.millete.investments.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;

import java.time.Instant;
import java.util.UUID;

public final class UserAsset {

    private final UUID id;
    private final UUID userId;
    private String name;
    private final AssetType type;
    private AssetSector sector;
    private final AssetOrigin origin;
    private final CurrencyCode currency;
    private final Instant createdAt;
    private Instant modifiedAt;

    private UserAsset(
            UUID id,
            UUID userId,
            String name,
            AssetType type,
            AssetSector sector,
            AssetOrigin origin,
            CurrencyCode currency,
            Instant createdAt,
            Instant modifiedAt
    ) {
        validateId(id);
        validateUserId(userId);
        validateName(name);
        validateType(type);
        validateSector(sector);
        validateOrigin(origin);
        validateCurrency(currency);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.sector = sector;
        this.origin = origin;
        this.currency = currency;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static UserAsset create(
            TimeProvider timeProvider,
            UUID userId,
            String name,
            AssetType type,
            AssetSector sector,
            AssetOrigin origin,
            CurrencyCode currency
    ) {
        Instant now = timeProvider.now();

        return new UserAsset(
                UUID.randomUUID(),
                userId,
                name,
                type,
                sector,
                origin,
                currency,
                now,
                now
        );
    }

    public static UserAsset reconstitute(
            UUID id,
            UUID userId,
            String name,
            AssetType type,
            AssetSector sector,
            AssetOrigin origin,
            CurrencyCode currency,
            Instant createdAt,
            Instant modifiedAt
    ) {
        return new UserAsset(
                id,
                userId,
                name,
                type,
                sector,
                origin,
                currency,
                createdAt,
                modifiedAt
        );
    }

    public void updateDetails(
            TimeProvider timeProvider,
            String name,
            AssetSector sector
    ) {
        validateName(name);
        validateSector(sector);

        this.name = name;
        this.sector = sector;
        this.modifiedAt = timeProvider.now();
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del UserAsset es obligatorio"
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El identificador del usuario es obligatorio"
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

    private static void validateType(AssetType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de Asset es obligatorio"
            );
        }
    }

    private static void validateSector(AssetSector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                    "El sector es obligatorio"
            );
        }
    }

    private static void validateOrigin(AssetOrigin origin) {
        if (origin == null) {
            throw new IllegalArgumentException(
                    "El origen del Asset es obligatorio"
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

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public AssetType getType() {
        return type;
    }

    public AssetSector getSector() {
        return sector;
    }

    public AssetOrigin getOrigin() {
        return origin;
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