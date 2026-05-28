package com.puntomartinez.millete.transactions.domain.ports.out;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    List<Transaction> findAllByUserId(UUID userId);
    List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDateTime start, LocalDateTime end);
    List<Transaction> findRecentByUserId(UUID userId, int limit);
    List<Transaction> findAllByCategoryId(UUID categoryId);
}