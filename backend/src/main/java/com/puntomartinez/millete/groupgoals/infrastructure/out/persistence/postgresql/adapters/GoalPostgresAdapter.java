package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalUnitEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import java.util.Collection;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoalPostgresAdapter implements GoalUnitRepository {

    private final JpaGoalUnitRepository jpaRepository;
    private final GoalUnitEntityMapper mapper;

    @Override
    public GoalUnit save(GoalUnit goalUnit) {
        var entity = mapper.toEntity(goalUnit);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<GoalUnit> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
public List<GoalUnit> findByIds(Collection<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
        return List.of();
    }
    return jpaRepository.findAllById(ids)
            .stream()
            .map(mapper::toDomain)
            .toList();
}

    @Override
    public List<GoalUnit> findByUserId(
            UUID userId,
            int page,
            int size) {

        return jpaRepository
                .findActiveByUserId(
                        userId,
                        PageRequest.of(page, size)
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(UUID userId) {
        return jpaRepository.countActiveByUserId(userId);
    }
}