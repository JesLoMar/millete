package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.mappers.TransactionEntityMapper;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.repository.SpringDataTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    private Specification<TransactionEntity> buildSpecification(UUID userId, String search, TransactionType type,
                                                               LocalDateTime startDate, LocalDateTime endDate) {
        Specification<TransactionEntity> spec = (root, query, cb) -> cb.equal(root.get("userId"), userId);
        spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), true));

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("description")), pattern));
        }

        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type.name()));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate));
        }

        return spec;
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

    @Override
    public List<Transaction> findAllByUserId(UUID userId, int page, int size, String search, TransactionType type,
                                               LocalDateTime startDate, LocalDateTime endDate) {
        Specification<TransactionEntity> spec = buildSpecification(userId, search, type, startDate, endDate);
        Page<TransactionEntity> result = repository.findAll(spec,
                PageRequest.of(page, size, Sort.by("date").descending()));
        return result.getContent().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndFilters(UUID userId, String search, TransactionType type,
                                        LocalDateTime startDate, LocalDateTime endDate) {
        return repository.count(buildSpecification(userId, search, type, startDate, endDate));
    }
}
