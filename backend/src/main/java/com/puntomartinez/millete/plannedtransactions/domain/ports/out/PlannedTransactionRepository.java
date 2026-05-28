package com.puntomartinez.millete.plannedtransactions.domain.ports.out;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlannedTransactionRepository {
    PlannedTransaction save(PlannedTransaction plannedTransaction);
    Optional<PlannedTransaction> findById(UUID id);
    List<PlannedTransaction> findAllByUserId(UUID userId);
    List<PlannedTransaction> findAllActive();
}