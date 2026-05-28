package com.puntomartinez.millete.transactions.domain.ports.in;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import java.util.List;
import java.util.UUID;

public interface ListTransactionsUseCase {

    List<Transaction> findAllByUserId(UUID userId);
}