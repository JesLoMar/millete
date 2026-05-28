package com.puntomartinez.millete.transactions.domain.ports.in;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UpdateTransactionUseCase {
    Transaction update(UUID id, UpdateTransactionCommand command);

    record UpdateTransactionCommand(
            UUID userId,
            BigDecimal amount,
            LocalDateTime date,
            Transaction.TransactionType type,
            String description,
            UUID categoryId
    ) {}
}