package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;

public record TradeActivityDetails(
        BigDecimal quantity,
        Money unitPrice,
        TradeSettlement settlement
) implements ActivityDetails {

    public static final int AMOUNT_SCALE = 12;

    public TradeActivityDetails {
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);
        validateSettlement(settlement);
    }

    public Money amount() {
        return unitPrice.multiply(
                quantity,
                AMOUNT_SCALE
        );
    }

    private static void validateQuantity(
            BigDecimal quantity
    ) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser positiva"
            );
        }
    }

    private static void validateUnitPrice(
            Money unitPrice
    ) {
        if (unitPrice == null) {
            throw new IllegalArgumentException(
                    "El precio unitario es obligatorio"
            );
        }

        if (unitPrice.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El precio unitario debe ser positivo"
            );
        }
    }

    private static void validateSettlement(
            TradeSettlement settlement
    ) {
        if (settlement == null) {
            throw new IllegalArgumentException(
                    "La liquidación de la operación es obligatoria"
            );
        }
    }
}