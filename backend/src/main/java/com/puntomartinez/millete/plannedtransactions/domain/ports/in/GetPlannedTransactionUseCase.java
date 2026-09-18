package com.puntomartinez.millete.plannedtransactions.domain.ports.in;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;

import java.util.UUID;

public interface GetPlannedTransactionUseCase {

    PlannedTransaction getByIdAndUserId(UUID id, UUID userId);
}