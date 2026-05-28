package com.puntomartinez.millete.transactions.domain.ports.in;

import java.util.UUID;

public interface DeleteTransactionUseCase {
    void deleteByIdAndUserId(UUID id, UUID userId);
}