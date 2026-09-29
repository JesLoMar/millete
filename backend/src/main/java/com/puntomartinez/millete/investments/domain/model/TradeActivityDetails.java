package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TradeActivityDetails(
        BigDecimal quantity,
        Money unitPrice
) implements ActivityDetails {

    public static final int AMOUNT_SCALE = 12;

    public TradeActivityDetails {
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);
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
}