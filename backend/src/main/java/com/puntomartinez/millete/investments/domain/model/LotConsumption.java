package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record LotConsumption(
        UUID id,
        UUID sellActivityId,
        UUID lotId,
        BigDecimal quantity,
        Money costBasis
) {

    public LotConsumption {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del consumo es obligatorio"
            );
        }

        if (sellActivityId == null) {
            throw new IllegalArgumentException(
                    "La venta es obligatoria"
            );
        }

        if (lotId == null) {
            throw new IllegalArgumentException(
                    "El Lot es obligatorio"
            );
        }

        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad consumida debe ser positiva"
            );
        }

        if (costBasis == null
                || costBasis.amount().signum() < 0) {

            throw new IllegalArgumentException(
                    "El coste consumido no puede ser negativo"
            );
        }
    }
}