package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.mappers.TransactionEntityMapper;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.repository.SpringDataTransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionPostgresAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository repository;
    private final TransactionEntityMapper mapper;

    public TransactionPostgresAdapter(SpringDataTransactionRepository repository, TransactionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entityToSave = mapper.toEntity(transaction);
        TransactionEntity savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findAllByUserId(UUID userId) {
        return repository.findAllByUserIdOrderByDateDesc(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDateTime start, LocalDateTime end) {
        return repository.findByUserIdAndDateBetween(userId, start, end).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findRecentByUserId(UUID userId, int limit) {
        return repository.findTop5ByUserIdOrderByDateDesc(userId).stream()
                .limit(limit)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findAllByCategoryId(UUID categoryId) {
        return repository.findAllByCategoryId(categoryId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}