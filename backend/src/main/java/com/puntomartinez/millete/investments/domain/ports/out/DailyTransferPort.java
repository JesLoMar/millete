package com.puntomartinez.millete.investments.domain.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public interface DailyTransferPort {
    UUID createTransfer(UUID userId, UUID activityId, TransferDirection direction,
                        BigDecimal localAmount, String localCurrency,
                        Instant occurredAt, String description);

    boolean existsTransfer(UUID transactionId, UUID userId);
    boolean matchesTransfer(UUID userId, UUID activityId, UUID transactionId,
                           TransferDirection direction, BigDecimal localAmount,
                           String localCurrency);

    enum TransferDirection { TRANSFER_IN, TRANSFER_OUT }
}
