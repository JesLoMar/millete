package com.puntomartinez.millete.plannedtransactions.domain.ports.in;

import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;

import java.util.List;
import java.util.UUID;

public interface ListPlannedTransactionsUseCase {

    List<PlannedTransaction> findAllByUserId(UUID userId, int page, int size, String search, TransactionType type);

    long countByUserIdAndFilters(UUID userId, String search, TransactionType type);
}
