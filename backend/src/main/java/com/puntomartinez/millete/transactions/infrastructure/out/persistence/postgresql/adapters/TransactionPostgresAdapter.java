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

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionPostgresAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository repository;
    private final TransactionEntityMapper mapper;

    public TransactionPostgresAdapter(
            SpringDataTransactionRepository repository,
            TransactionEntityMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    private Specification<TransactionEntity> buildSpecification(
            UUID userId,
            String search,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));
            predicates.add(cb.equal(root.get("active"), true));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";

                predicates.add(
                        cb.like(
                                cb.lower(root.get("description")),
                                pattern
                        )
                );
            }

            if (type != null) {
                predicates.add(
                        cb.equal(root.get("type"), type.name())
                );
            }

            if (startDate != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("date"),
                                startDate
                        )
                );
            }

            if (endDate != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("date"),
                                endDate
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
    public Optional<Transaction> findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean isInvestmentManaged(UUID id, UUID userId) {
        return repository.existsByIdAndUserIdAndInvestmentActivityIdIsNotNull(id, userId);
    }

    @Override
    public List<Transaction> findAllByUserId(UUID userId) {
        return repository.findAllByUserIdOrderByDateDesc(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByUserIdAndDateBetween(
            UUID userId,
            LocalDate start,
            LocalDate end
    ) {
        return repository.findByUserIdAndDateBetween(
                        userId,
                        start,
                        end
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findRecentByUserId(UUID userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return repository.findByUserIdAndActiveTrueOrderByDateDesc(
                        userId,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void clearCategoryFromActiveTransactions(
            UUID categoryId,
            UUID userId,
            Instant modifiedAt
    ) {
        repository.clearCategoryFromActiveTransactions(
                categoryId,
                userId,
                modifiedAt
        );
    }

    @Override
    public List<Transaction> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Specification<TransactionEntity> spec =
                buildSpecification(
                        userId,
                        search,
                        type,
                        startDate,
                        endDate
                );

        Page<TransactionEntity> result = repository.findAll(
                spec,
                PageRequest.of(
                        page,
                        size,
                        Sort.by("date").descending()
                )
        );

        return result.getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndFilters(
            UUID userId,
            String search,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return repository.count(
                buildSpecification(
                        userId,
                        search,
                        type,
                        startDate,
                        endDate
                )
        );
    }

    @Override
    public TransactionAggregates getAggregatesByUserIdAndDateBetween(
            UUID userId,
            LocalDate start,
            LocalDate end
    ) {
        List<Object[]> results =
                repository.getAggregatesByUserIdAndDateBetween(
                        userId,
                        start,
                        end
                );

        Object[] result = results.getFirst();

        BigDecimal totalIncome = toBigDecimal(result[0]);
        BigDecimal totalExpense = toBigDecimal(result[1]);

        long count = result[2] != null
                ? ((Number) result[2]).longValue()
                : 0L;

        BigDecimal transferIn = toBigDecimal(result[3]);
        BigDecimal transferOut = toBigDecimal(result[4]);

        return new TransactionAggregates(
                totalIncome,
                totalExpense,
                count,
                transferIn,
                transferOut
        );
    }

    private BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> BigDecimal.ZERO;
            case BigDecimal bigDecimal -> bigDecimal;
            case Number number -> new BigDecimal(number.toString());
            default -> throw new IllegalStateException(
                    "El resultado del agregado no es numérico: "
                            + value.getClass().getName()
            );
        };
    }
}
