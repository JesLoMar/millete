package com.puntomartinez.millete.plannedtransactions.domain.ports.out;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlannedTransactionRepository {
    PlannedTransaction save(PlannedTransaction plannedTransaction);
    Optional<PlannedTransaction> findById(UUID id);
    List<PlannedTransaction> findAllByUserId(UUID userId);
    List<PlannedTransaction> findAllActive();

    List<PlannedTransaction> findAllByUserId(UUID userId, int page, int size, String search, TransactionType type);

    long countByUserIdAndFilters(UUID userId, String search, TransactionType type);
}
