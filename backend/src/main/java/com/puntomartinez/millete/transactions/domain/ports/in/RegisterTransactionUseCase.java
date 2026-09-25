package com.puntomartinez.millete.transactions.domain.ports.in;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface RegisterTransactionUseCase {


    RegisterTransactionResult register(RegisterTransactionCommand command);

    record RegisterTransactionCommand(
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            LocalDate date,
            Transaction.TransactionType type,
            String description
    ) {}

    record RegisterTransactionResult(
            Transaction transaction,
            boolean limitExceeded
    ) {}
}
