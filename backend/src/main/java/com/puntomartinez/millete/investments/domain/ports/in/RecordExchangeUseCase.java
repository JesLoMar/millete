package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecordExchangeUseCase {

    Activity record(
            UUID userId,
            RecordExchangeCommand command,
            String idempotencyKey
    );

    record RecordExchangeCommand(
            Instant occurredAt,
            BigDecimal amountOrigin,
            String currencyOrigin,
            String currencyDestination,
            String comment
    ) {
    }
}