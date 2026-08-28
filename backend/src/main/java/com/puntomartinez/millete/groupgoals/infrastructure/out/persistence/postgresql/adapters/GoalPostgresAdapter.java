package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalUnitEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<GoalUnit> findByUserId(UUID userId, int page, int size) {
        return jpaRepository.findActiveByUserId(userId, PageRequest.of(page, size, Sort.by("name"))).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(UUID userId) {
        return jpaRepository.findActiveByUserId(userId, PageRequest.of(0, 1)).getTotalElements();
    }
}
