package com.puntomartinez.millete.investments.domain.model;

import java.math.BigDecimal;

public record SplitActivityDetails(
        BigDecimal ratio
) implements ActivityDetails {

    public SplitActivityDetails {
        if (ratio == null || ratio.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El ratio del split debe ser positivo"
            );
        }
    }
}