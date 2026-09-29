package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.SettlementCurrency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReplaceHoldingWithHistoryUseCase {

    List<Activity> replace(
            UUID userId,
            UUID holdingId,
            ReplaceHoldingWithHistoryCommand command
    );

    record ReplaceHoldingWithHistoryCommand(
            List<HistoricalActivity> history
    ) {
    }

    record HistoricalActivity(
            Instant occurredAt,
            HistoricalActivityDetails details
    ) {
    }

    interface HistoricalActivityDetails {
    }

    record BuyDetails(
            BigDecimal quantity,
            BigDecimal unitPrice,
            SettlementCurrency settlementCurrency,
            String comment
    ) implements HistoricalActivityDetails {
    }

    record SellDetails(
            BigDecimal quantity,
            BigDecimal unitPrice,
            SettlementCurrency settlementCurrency,
            String comment
    ) implements HistoricalActivityDetails {
    }

    record CashDividendDetails(
            BigDecimal amount,
            String currency,
            String comment
    ) implements HistoricalActivityDetails {
    }

    record InKindDividendDetails(
            BigDecimal quantity,
            String comment
    ) implements HistoricalActivityDetails {
    }

    record SplitDetails(
            BigDecimal ratio,
            String comment
    ) implements HistoricalActivityDetails {
    }
}