package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;

public record OpeningPositionDetails(
        BigDecimal quantity,
        Money acquisitionCost
) implements ActivityDetails {

    public OpeningPositionDetails {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser positiva"
            );
        }

        if (acquisitionCost == null) {
            throw new IllegalArgumentException(
                    "El coste de adquisición es obligatorio"
            );
        }

        if (acquisitionCost.amount().signum() < 0) {
            throw new IllegalArgumentException(
                    "El coste de adquisición no puede ser negativo"
            );
        }
    }
}