package com.puntomartinez.millete.plannedtransactions.domain.ports.in;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface UpdatePlannedTransactionUseCase {
    PlannedTransaction update(UUID id, UpdatePlannedTransactionCommand command);

    record UpdatePlannedTransactionCommand(
            UUID userId,
            UUID categoryId,
            BigDecimal amount,
            TransactionType type,
            String description,
            PlannedTransaction.FrequencyType frequencyType,
            Integer frequencyInterval,
            LocalDate startDate,
            LocalDate endDate
    ) {}
}