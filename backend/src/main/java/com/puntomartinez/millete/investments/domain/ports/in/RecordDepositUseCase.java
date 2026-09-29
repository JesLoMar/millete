package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecordDepositUseCase {

    Activity record(
            UUID userId,
            RecordDepositCommand command,
            String idempotencyKey
    );

    record RecordDepositCommand(
            Instant occurredAt,
            BigDecimal amount,
            String currency,
            String comment
    ) {
    }
}