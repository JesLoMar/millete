package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ExchangeActivityDetails(
        Money origin,
        Money destination,
        AppliedFxRate appliedFxRate
) implements ActivityDetails {

    public static final int AMOUNT_SCALE = 12;

    public ExchangeActivityDetails {
        if (origin == null) {
            throw new IllegalArgumentException(
                    "El importe origen es obligatorio"
            );
        }

        if (destination == null) {
            throw new IllegalArgumentException(
                    "El importe destino es obligatorio"
            );
        }

        if (appliedFxRate == null) {
            throw new IllegalArgumentException(
                    "El tipo de cambio aplicado es obligatorio"
            );
        }

        if (origin.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El importe origen debe ser positivo"
            );
        }

        if (destination.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El importe destino debe ser positivo"
            );
        }

        if (origin.currency().equals(destination.currency())) {
            throw new IllegalArgumentException(
                    "Las monedas de una conversión deben ser distintas"
            );
        }

        if (!origin.currency().equals(
                appliedFxRate.baseCurrency()
        )) {
            throw new IllegalArgumentException(
                    "La moneda origen no coincide con el tipo de cambio"
            );
        }

        if (!destination.currency().equals(
                appliedFxRate.quoteCurrency()
        )) {
            throw new IllegalArgumentException(
                    "La moneda destino no coincide con el tipo de cambio"
            );
        }

        BigDecimal expectedAmount =
                origin.amount()
                        .multiply(appliedFxRate.rate())
                        .setScale(
                                AMOUNT_SCALE,
                                RoundingMode.HALF_UP
                        );

        BigDecimal recordedAmount =
                destination.amount()
                        .setScale(
                                AMOUNT_SCALE,
                                RoundingMode.HALF_UP
                        );

        if (expectedAmount.compareTo(recordedAmount) != 0) {
            throw new IllegalArgumentException(
                    "El importe destino no coincide con el importe "
                            + "origen multiplicado por el tipo de cambio"
            );
        }
    }

    public static ExchangeActivityDetails create(
            Money origin,
            AppliedFxRate appliedFxRate
    ) {
        BigDecimal destinationAmount =
                origin.amount()
                        .multiply(appliedFxRate.rate())
                        .setScale(
                                AMOUNT_SCALE,
                                RoundingMode.HALF_UP
                        );

        Money destination = new Money(
                destinationAmount,
                appliedFxRate.quoteCurrency()
        );

        return new ExchangeActivityDetails(
                origin,
                destination,
                appliedFxRate
        );
    }
}