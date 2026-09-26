package com.puntomartinez.millete.investments.domain.model;

import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

public record UserLocalCurrencyPeriod(UUID id, UUID userId, String currency,
                                      Instant validFrom, Instant validTo,
                                      boolean inferred) {
    public UserLocalCurrencyPeriod {
        if (id == null || userId == null || validFrom == null) throw new IllegalArgumentException("Currency history identifiers and validFrom are required");
        if (validTo != null && !validTo.isAfter(validFrom)) throw new IllegalArgumentException("validTo must be after validFrom");
        if (currency == null || !currency.matches("[A-Za-z]{3}")) throw new IllegalArgumentException("Currency must be ISO 4217");
        currency = currency.toUpperCase(Locale.ROOT);
        Currency.getInstance(currency);
    }
}
