package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.SettlementCurrency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface EditActivityUseCase {

    Activity edit(
            UUID userId,
            UUID activityId,
            EditActivityCommand command
    );

    record EditActivityCommand(
            Instant occurredAt,
            ActivityEditDetails details,
            String reason
    ) {
    }

    interface ActivityEditDetails {
    }

    record BuySellDetails(
            BigDecimal quantity,
            BigDecimal unitPrice,
            SettlementCurrency settlementCurrency,
            String comment
    ) implements ActivityEditDetails {
    }

    record CashDividendDetails(
            BigDecimal amount,
            String comment
    ) implements ActivityEditDetails {
    }

    record InKindDividendDetails(
            BigDecimal quantity,
            String comment
    ) implements ActivityEditDetails {
    }

    record ExchangeDetails(
            BigDecimal amountOrigin,
            String comment
    ) implements ActivityEditDetails {
    }

    record SplitDetails(
            BigDecimal ratio,
            String comment
    ) implements ActivityEditDetails {
    }
}