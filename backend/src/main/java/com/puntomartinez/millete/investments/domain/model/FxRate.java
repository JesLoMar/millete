package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public record FxRate(
        UUID id,
        CurrencyCode baseCurrency,
        CurrencyCode quoteCurrency,
        Instant timestamp,
        BigDecimal rate,
        String source,
        Instant fetchedAt
) {

    public FxRate {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del FX es obligatorio"
            );
        }

        if (baseCurrency == null
                || quoteCurrency == null) {

            throw new IllegalArgumentException(
                    "Las monedas del FX son obligatorias"
            );
        }

        if (baseCurrency.equals(quoteCurrency)) {
            throw new IllegalArgumentException(
                    "Las monedas del FX deben ser distintas"
            );
        }

        if (timestamp == null || fetchedAt == null) {
            throw new IllegalArgumentException(
                    "Las fechas del FX son obligatorias"
            );
        }

        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El tipo de cambio debe ser positivo"
            );
        }

        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException(
                    "La fuente del FX es obligatoria"
            );
        }

        source = source.trim();
    }

    /**
     * Devuelve el mismo tipo de cambio expresado en sentido inverso.
     *
     * Si este FX representa 1 unidad de {@code baseCurrency} en unidades de
     * {@code quoteCurrency}, el resultado representa 1 unidad de
     * {@code quoteCurrency} en unidades de {@code baseCurrency}. Sirve para
     * reutilizar un FX guardado como USD/EUR cuando la operación necesita
     * EUR/USD.
     */
    public FxRate inverse() {
        return new FxRate(
                id,
                quoteCurrency,
                baseCurrency,
                timestamp,
                BigDecimal.ONE.divide(
                        rate,
                        12,
                        RoundingMode.HALF_UP
                ),
                source,
                fetchedAt
        );
    }
}