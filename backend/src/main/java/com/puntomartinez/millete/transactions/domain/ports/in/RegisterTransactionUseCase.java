package com.puntomartinez.millete.transactions.domain.ports.in;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterTransactionUseCase {

    // Ahora devolvemos nuestro nuevo Record de resultado
    RegisterTransactionResult register(RegisterTransactionCommand command);

    record RegisterTransactionCommand(
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            LocalDateTime date,
            Transaction.TransactionType type,
            String description
    ) {}

    record RegisterTransactionResult(
            Transaction transaction,
            boolean limitExceeded
    ) {}
}