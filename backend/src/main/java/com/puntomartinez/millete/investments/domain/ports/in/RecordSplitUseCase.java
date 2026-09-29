package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecordSplitUseCase {

    Activity record(
            UUID userId,
            RecordSplitCommand command,
            String idempotencyKey
    );

    record RecordSplitCommand(
            Instant occurredAt,
            AssetReference assetReference,
            BigDecimal ratio,
            String comment
    ) {
    }
}