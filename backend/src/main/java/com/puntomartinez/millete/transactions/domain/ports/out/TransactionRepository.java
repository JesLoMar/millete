package com.puntomartinez.millete.transactions.domain.ports.out;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID id);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    boolean isInvestmentManaged(UUID id, UUID userId);

    List<Transaction> findAllByUserId(UUID userId);

    List<Transaction> findByUserIdAndDateBetween(
            UUID userId,
            LocalDate start,
            LocalDate end
    );

    List<Transaction> findRecentByUserId(
            UUID userId,
            int limit
    );

    void clearCategoryFromActiveTransactions(
            UUID categoryId,
            UUID userId,
            Instant modifiedAt
    );

    List<Transaction> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );

    long countByUserIdAndFilters(
            UUID userId,
            String search,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );

    TransactionAggregates getAggregatesByUserIdAndDateBetween(
            UUID userId,
            LocalDate start,
            LocalDate end
    );

    record TransactionAggregates(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            long count,
            BigDecimal transferIn,
            BigDecimal transferOut
    ) {
    }
}
