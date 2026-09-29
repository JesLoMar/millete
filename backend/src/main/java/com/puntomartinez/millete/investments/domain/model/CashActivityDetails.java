package com.puntomartinez.millete.investments.domain.model;

public record CashActivityDetails(
        Money amount
) implements ActivityDetails {

    public CashActivityDetails {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "El importe es obligatorio"
            );
        }

        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El importe debe ser positivo"
            );
        }
    }
}