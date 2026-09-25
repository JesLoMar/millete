package com.puntomartinez.millete.transactions.domain.ports.in;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListTransactionsUseCase {

    List<Transaction> findAllByUserId(UUID userId);

    List<Transaction> findAllByUserId(UUID userId, int page, int size, String search, TransactionType type,
                                        LocalDate startDate, LocalDate endDate);

    long countByUserIdAndFilters(UUID userId, String search, TransactionType type,
                                 LocalDate startDate, LocalDate endDate);
}
