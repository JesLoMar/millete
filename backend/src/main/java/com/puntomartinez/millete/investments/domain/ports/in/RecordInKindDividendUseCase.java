package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecordInKindDividendUseCase {

    Activity record(
            UUID userId,
            RecordInKindDividendCommand command,
            String idempotencyKey
    );

    record RecordInKindDividendCommand(
            Instant occurredAt,
            AssetReference assetReference,
            BigDecimal quantity,
            String comment
    ) {
    }
}