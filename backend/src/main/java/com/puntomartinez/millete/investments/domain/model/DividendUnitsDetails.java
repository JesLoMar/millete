package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;

public record DividendUnitsDetails(
        BigDecimal quantity,
        Money referenceUnitPrice
) implements ActivityDetails {

    public DividendUnitsDetails {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad del dividendo debe ser positiva"
            );
        }

        if (referenceUnitPrice == null) {
            throw new IllegalArgumentException(
                    "El precio de referencia del dividendo es obligatorio"
            );
        }

        if (referenceUnitPrice.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El precio de referencia debe ser positivo"
            );
        }
    }
}