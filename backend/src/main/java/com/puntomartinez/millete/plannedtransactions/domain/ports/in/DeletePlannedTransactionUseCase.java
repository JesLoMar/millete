package com.puntomartinez.millete.plannedtransactions.domain.ports.in;

import java.util.UUID;

public interface DeletePlannedTransactionUseCase {
    void deleteByIdAndUserId(UUID id, UUID userId);
}