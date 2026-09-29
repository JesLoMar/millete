package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecordWithdrawalUseCase {

    Activity record(
            UUID userId,
            RecordWithdrawalCommand command,
            String idempotencyKey
    );

    record RecordWithdrawalCommand(
            Instant occurredAt,
            BigDecimal amount,
            String currency,
            String comment
    ) {
    }
}