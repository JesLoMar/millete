package com.puntomartinez.millete.transactions.domain.ports.out;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID id);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findAllByUserId(UUID userId);

    List<Transaction> findByUserIdAndDateBetween(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Transaction> findRecentByUserId(
            UUID userId,
            int limit
    );

    void clearCategoryFromActiveTransactions(
            UUID categoryId,
            UUID userId,
            LocalDateTime modifiedAt
    );

    List<Transaction> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countByUserIdAndFilters(
            UUID userId,
            String search,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    TransactionAggregates getAggregatesByUserIdAndDateBetween(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    );

    record TransactionAggregates(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            long count
    ) {
    }
}