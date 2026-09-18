package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.mappers.PlannedTransactionEntityMapper;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.repository.JpaPlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PlannedTransactionPostgresAdapter
        implements PlannedTransactionRepository {

    private final JpaPlannedTransactionRepository repository;
    private final PlannedTransactionEntityMapper mapper;

    public PlannedTransactionPostgresAdapter(
            JpaPlannedTransactionRepository repository,
            PlannedTransactionEntityMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    private Specification<PlannedTransactionEntity> buildSpecification(
            UUID userId,
            String search,
            TransactionType type
    ) {
        Specification<PlannedTransactionEntity> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(
                                root.get("userId"),
                                userId
                        );

        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.isTrue(root.get("active"))
        );

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(
                                            root.get("description")
                                    ),
                                    pattern
                            )
            );
        }

        if (type != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("type"),
                                    type.name()
                            )
            );
        }

        return specification;
    }

    @Override
    public PlannedTransaction save(
            PlannedTransaction plannedTransaction
    ) {
        PlannedTransactionEntity entity =
                mapper.toEntity(plannedTransaction);
        PlannedTransactionEntity savedEntity =
                repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PlannedTransaction> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<PlannedTransaction> findAllByUserId(
            UUID userId
    ) {
        return repository
                .findByUserIdAndActiveTrueOrderByStartDateDesc(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PlannedTransaction> findAllActive(
            int page,
            int size,
            LocalDate today
    ) {
        return repository
                .findByActiveTrueAndNotExpired(
                        today,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by("id").ascending()
                        )
                )
                .getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PlannedTransaction> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search,
            TransactionType type
    ) {
        Specification<PlannedTransactionEntity> specification =
                buildSpecification(userId, search, type);

        Page<PlannedTransactionEntity> result =
                repository.findAll(
                        specification,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by("startDate").descending()
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
            TransactionType type
    ) {
        return repository.count(
                buildSpecification(
                        userId,
                        search,
                        type
                )
        );
    }
}