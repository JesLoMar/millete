package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

public interface TransactionIntegrationPort {

    UUID createInvestmentTransfer(
            UUID userId,
            UUID activityId,
            TransferDirection direction,
            Money localAmount,
            Instant occurredAt,
            String description
    );

    boolean matchesInvestmentTransfer(
            UUID userId,
            UUID activityId,
            UUID transactionId,
            TransferDirection direction,
            Money localAmount
    );

    enum TransferDirection {
        TRANSFER_IN,
        TRANSFER_OUT
    }
}