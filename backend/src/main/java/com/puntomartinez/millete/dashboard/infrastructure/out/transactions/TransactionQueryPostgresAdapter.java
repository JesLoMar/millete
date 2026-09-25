package com.puntomartinez.millete.dashboard.infrastructure.out.transactions;

import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionQueryPostgresAdapter implements TransactionQueryPort {

    private final TransactionRepository transactionRepository;

    public TransactionQueryPostgresAdapter(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<TransactionData> findByUserIdAndDateBetween(
            UUID userId,
            LocalDate start,
            LocalDate end
    ) {
        return transactionRepository
                .findByUserIdAndDateBetween(userId, start, end)
                .stream()
                .map(this::toTransactionData)
                .toList();
    }

    @Override
    public List<TransactionData> findRecentByUserId(
            UUID userId,
            int limit
    ) {
        return transactionRepository
                .findRecentByUserId(userId, limit)
                .stream()
                .map(this::toTransactionData)
                .toList();
    }

    private TransactionData toTransactionData(Transaction transaction) {
        return new TransactionData(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getCategoryId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType().name()
        );
    }
}