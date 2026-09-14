package com.puntomartinez.millete.dataexport.infrastructure.out.transactions;

import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.TransactionExportPort;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionExportAdapter implements TransactionExportPort {

    private final TransactionRepository transactionRepository;

    public TransactionExportAdapter(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<TransactionSnapshot> findAllByUserId(UUID userId) {
        return transactionRepository.findAllByUserId(userId)
                .stream()
                .map(transaction ->
                        new TransactionSnapshot(
                                transaction.getId(),
                                transaction.getUserId(),
                                transaction.getCategoryId(),
                                transaction.getAmount(),
                                transaction.getDate(),
                                transaction.getType().name(),
                                transaction.getDescription(),
                                transaction.getCreatedAt(),
                                transaction.getModifiedAt(),
                                transaction.isActive()
                        )
                )
                .toList();
    }

    @Override
    public List<TransactionSnapshot> findByUserIdAndDateBetween(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return transactionRepository
                .findByUserIdAndDateBetween(userId, start, end)
                .stream()
                .map(transaction ->
                        new TransactionSnapshot(
                                transaction.getId(),
                                transaction.getUserId(),
                                transaction.getCategoryId(),
                                transaction.getAmount(),
                                transaction.getDate(),
                                transaction.getType().name(),
                                transaction.getDescription(),
                                transaction.getCreatedAt(),
                                transaction.getModifiedAt(),
                                transaction.isActive()
                        )
                )
                .toList();
    }
}