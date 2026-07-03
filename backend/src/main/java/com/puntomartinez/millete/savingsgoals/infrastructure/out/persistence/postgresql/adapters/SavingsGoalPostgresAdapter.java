package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.mappers.SavingsGoalEntityMapper;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.repository.JpaSavingsGoalRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SavingsGoalPostgresAdapter implements SavingsGoalRepository {

    private final JpaSavingsGoalRepository jpaRepository;
    private final SavingsGoalEntityMapper mapper;

    public SavingsGoalPostgresAdapter(JpaSavingsGoalRepository jpaRepository, SavingsGoalEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SavingsGoal save(SavingsGoal savingsGoal) {
        SavingsGoalEntity entity = mapper.toEntity(savingsGoal);
        SavingsGoalEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SavingsGoal> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SavingsGoal> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public List<SavingsGoal> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserIdAndActiveTrue(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SavingsGoal> findAllByUserIdAndStatus(UUID userId, String status) {
        return jpaRepository.findByUserIdAndStatusAndActiveTrue(userId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
