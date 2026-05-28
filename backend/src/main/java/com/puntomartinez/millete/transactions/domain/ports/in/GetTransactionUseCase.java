package com.puntomartinez.millete.transactions.domain.ports.in;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import java.util.UUID;

public interface GetTransactionUseCase {
    Transaction getByIdAndUserId(UUID id, UUID userId);
}