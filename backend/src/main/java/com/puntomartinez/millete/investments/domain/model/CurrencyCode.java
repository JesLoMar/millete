package com.puntomartinez.millete.investments.domain.model;

import java.util.Currency;
import java.util.Locale;

public record CurrencyCode(String value) {

    public CurrencyCode {
        if (value == null || !value.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException(
                    "La moneda debe ser un código ISO 4217"
            );
        }

        value = value.toUpperCase(Locale.ROOT);
        Currency.getInstance(value);
    }

    public static CurrencyCode of(String value) {
        return new CurrencyCode(value);
    }
}