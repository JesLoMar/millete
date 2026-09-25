package com.puntomartinez.millete.dashboard.domain.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionQueryPort {

    List<TransactionData> findByUserIdAndDateBetween(
            UUID userId,
            LocalDate start,
            LocalDate end
    );

    List<TransactionData> findRecentByUserId(
            UUID userId,
            int limit
    );

    record TransactionData(
            UUID id,
            String description,
            UUID categoryId,
            BigDecimal amount,
            LocalDate date,
            String type
    ) {
    }
}