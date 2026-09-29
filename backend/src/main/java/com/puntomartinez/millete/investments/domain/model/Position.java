package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Position(
        UUID userId,
        AssetReference assetReference,
        BigDecimal quantity,
        Money acquisitionCost,
        boolean historyIncomplete,
        boolean estimated
) {

    public Position {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (assetReference == null) {
            throw new IllegalArgumentException(
                    "El AssetReference es obligatorio"
            );
        }

        if (quantity == null
                || quantity.signum() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad de la Position debe ser positiva"
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

        if (historyIncomplete && !estimated) {
            throw new IllegalArgumentException(
                    "historyIncomplete implica estimated=true"
            );
        }
    }

    public boolean isOpen() {
        return quantity.signum() > 0;
    }
}