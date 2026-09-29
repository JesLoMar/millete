package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record AppliedFxRate(
        CurrencyCode baseCurrency,
        CurrencyCode quoteCurrency,
        BigDecimal rate,
        String source,
        Instant timestamp
) {

    public AppliedFxRate {
        if (baseCurrency == null) {
            throw new IllegalArgumentException(
                    "La moneda origen es obligatoria"
            );
        }

        if (quoteCurrency == null) {
            throw new IllegalArgumentException(
                    "La moneda destino es obligatoria"
            );
        }

        if (baseCurrency.equals(quoteCurrency)) {
            throw new IllegalArgumentException(
                    "Las monedas del tipo de cambio deben ser distintas"
            );
        }

        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El tipo de cambio debe ser positivo"
            );
        }

        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException(
                    "La fuente del tipo de cambio es obligatoria"
            );
        }

        if (timestamp == null) {
            throw new IllegalArgumentException(
                    "El instante del tipo de cambio es obligatorio"
            );
        }

        source = source.trim();
    }
}