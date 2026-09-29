package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.SettlementCurrency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecordSellUseCase {

    Activity record(
            UUID userId,
            RecordSellCommand command,
            String idempotencyKey
    );

    record RecordSellCommand(
            Instant occurredAt,
            AssetReference assetReference,
            BigDecimal quantity,
            BigDecimal unitPrice,
            SettlementCurrency settlementCurrency,
            String comment
    ) {
    }
}