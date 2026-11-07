package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecordBuyUseCase {

    Activity record(
            UUID userId,
            RecordBuyCommand command,
            String idempotencyKey
    );

    record RecordBuyCommand(
            Instant occurredAt,
            AssetReference assetReference,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String comment
    ) {
    }
}