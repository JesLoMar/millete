package com.puntomartinez.millete.investments.domain.model;

public record PositionValuation(
        CalculationStatus status,
        Money value,
        boolean estimated,
        String unavailableReason
) {

    public PositionValuation {
        if (status == null) {
            throw new IllegalArgumentException(
                    "El estado del cálculo es obligatorio"
            );
        }

        if (status == CalculationStatus.CALCULABLE) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Una valoración calculable debe tener valor"
                );
            }

            if (unavailableReason != null
                    && !unavailableReason.isBlank()) {

                throw new IllegalArgumentException(
                        "Una valoración calculable no puede tener unavailableReason"
                );
            }
        }

        if (status == CalculationStatus.NOT_CALCULABLE) {
            if (value != null) {
                throw new IllegalArgumentException(
                        "Una valoración no calculable no puede tener valor"
                );
            }

            if (estimated) {
                throw new IllegalArgumentException(
                        "Una valoración no calculable no puede estar estimated"
                );
            }

            if (unavailableReason == null
                    || unavailableReason.isBlank()) {

                throw new IllegalArgumentException(
                        "Una valoración no calculable requiere unavailableReason"
                );
            }
        }

        unavailableReason =
                unavailableReason == null
                        ? null
                        : unavailableReason.trim();
    }

    public static PositionValuation calculable(
            Money value,
            boolean estimated
    ) {
        return new PositionValuation(
                CalculationStatus.CALCULABLE,
                value,
                estimated,
                null
        );
    }

    public static PositionValuation notCalculable(
            String reason
    ) {
        return new PositionValuation(
                CalculationStatus.NOT_CALCULABLE,
                null,
                false,
                reason
        );
    }
}