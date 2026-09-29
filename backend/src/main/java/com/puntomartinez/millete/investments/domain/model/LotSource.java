package com.puntomartinez.millete.investments.domain.model;

import java.util.UUID;

public record LotSource(
        LotSourceType type,
        UUID id
) {

    public LotSource {
        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de origen del Lot es obligatorio"
            );
        }

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del origen es obligatorio"
            );
        }
    }

    public static LotSource activity(
            UUID activityId
    ) {
        return new LotSource(
                LotSourceType.ACTIVITY,
                activityId
        );
    }

    public static LotSource holding(
            UUID holdingId
    ) {
        return new LotSource(
                LotSourceType.HOLDING,
                holdingId
        );
    }
}