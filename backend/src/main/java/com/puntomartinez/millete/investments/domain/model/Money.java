package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(
        BigDecimal amount,
        CurrencyCode currency
) {

    public Money {
        Objects.requireNonNull(amount, "El importe es obligatorio");
        Objects.requireNonNull(currency, "La moneda es obligatoria");
    }

    public static Money of(
            BigDecimal amount,
            String currency
    ) {
        return new Money(
                amount,
                CurrencyCode.of(currency)
        );
    }

    public static Money zero(
            CurrencyCode currency
    ) {
        return new Money(
                BigDecimal.ZERO,
                currency
        );
    }

    public Money add(Money other) {
        requireSameCurrency(other);

        return new Money(
                amount.add(other.amount),
                currency
        );
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);

        return new Money(
                amount.subtract(other.amount),
                currency
        );
    }

    public Money multiply(
            BigDecimal multiplier,
            int scale
    ) {
        Objects.requireNonNull(
                multiplier,
                "El multiplicador es obligatorio"
        );

        return new Money(
                amount.multiply(multiplier)
                        .setScale(
                                scale,
                                RoundingMode.HALF_UP
                        ),
                currency
        );
    }

    public Money divide(
            BigDecimal divisor,
            int scale
    ) {
        Objects.requireNonNull(
                divisor,
                "El divisor es obligatorio"
        );

        if (divisor.signum() == 0) {
            throw new IllegalArgumentException(
                    "El divisor no puede ser cero"
            );
        }

        return new Money(
                amount.divide(
                        divisor,
                        scale,
                        RoundingMode.HALF_UP
                ),
                currency
        );
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(
                other,
                "El dinero es obligatorio"
        );

        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Las monedas deben coincidir"
            );
        }
    }
}