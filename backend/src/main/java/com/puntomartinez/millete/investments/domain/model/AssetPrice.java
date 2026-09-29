package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssetPrice(
        UUID id,
        UUID sharedAssetId,
        Instant timestamp,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal adjustedClose,
        BigDecimal volume,
        CurrencyCode currency,
        String source,
        Instant fetchedAt
) {

    public AssetPrice {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del precio es obligatorio"
            );
        }

        if (sharedAssetId == null) {
            throw new IllegalArgumentException(
                    "El SharedAsset es obligatorio"
            );
        }

        if (timestamp == null || fetchedAt == null) {
            throw new IllegalArgumentException(
                    "Las fechas del precio son obligatorias"
            );
        }

        if (currency == null) {
            throw new IllegalArgumentException(
                    "La moneda del precio es obligatoria"
            );
        }

        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException(
                    "La fuente del precio es obligatoria"
            );
        }

        validateNonNegative(open, "open");
        validateNonNegative(high, "high");
        validateNonNegative(low, "low");
        validateNonNegative(close, "close");
        validateNonNegative(adjustedClose, "adjustedClose");
        validateNonNegative(volume, "volume");

        source = source.trim();
    }

    public BigDecimal valuationPrice() {
        return close != null
                ? close
                : adjustedClose;
    }

    private static void validateNonNegative(
            BigDecimal value,
            String name
    ) {
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException(
                    name + " no puede ser negativo"
            );
        }
    }
}