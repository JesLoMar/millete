package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.mappers.PlannedTransactionEntityMapper;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.repository.SpringDataPlannedTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PlannedTransactionPostgresAdapter implements PlannedTransactionRepository {

    private final SpringDataPlannedTransactionRepository repository;
    private final PlannedTransactionEntityMapper mapper;

    public PlannedTransactionPostgresAdapter(SpringDataPlannedTransactionRepository repository,
                                             PlannedTransactionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PlannedTransaction save(PlannedTransaction plannedTransaction) {
        PlannedTransactionEntity entity = mapper.toEntity(plannedTransaction);
        PlannedTransactionEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PlannedTransaction> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PlannedTransaction> findAllByUserId(UUID userId) {
        return repository.findAllByUserIdOrderByStartDateDesc(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PlannedTransaction> findAllActive() {
        return repository.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }
}