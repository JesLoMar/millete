package com.puntomartinez.millete.investments.domain.model;

public record TradeSettlement(
        Money amount,
        AppliedFxRate appliedFxRate
) {

    public TradeSettlement {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "El importe de liquidación es obligatorio"
            );
        }

        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El importe de liquidación debe ser positivo"
            );
        }

        if (appliedFxRate != null
                && !amount.currency().equals(appliedFxRate.baseCurrency())
                && !amount.currency().equals(appliedFxRate.quoteCurrency())) {

            throw new IllegalArgumentException(
                    "La moneda de liquidación debe coincidir con una de las monedas del tipo de cambio"
            );
        }
    }

    public boolean converted() {
        return appliedFxRate != null;
    }
}