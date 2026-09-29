package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecordCashDividendUseCase {

    Activity record(
            UUID userId,
            RecordCashDividendCommand command,
            String idempotencyKey
    );

    record RecordCashDividendCommand(
            Instant occurredAt,
            AssetReference assetReference,
            BigDecimal amount,
            String currency,
            String comment
    ) {
    }
}