package com.puntomartinez.millete.plannedtransactions.domain.ports.in;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdatePlannedTransactionUseCase {

    PlannedTransaction update(
            UUID id,
            UUID userId,
            UpdatePlannedTransactionCommand command
    );

    record UpdatePlannedTransactionCommand(
            BigDecimal amount,
            TransactionType type,
            String description,
            PlannedTransaction.FrequencyType frequencyType,
            Integer frequencyInterval,
            UUID categoryId
    ) {
    }
}