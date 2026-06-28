package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalContributionEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalContributionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoalContributionPostgresAdapter implements GoalContributionRepository {

    private final JpaGoalContributionRepository jpaRepository;
    private final GoalContributionEntityMapper mapper;

    @Override
    public GoalContribution save(GoalContribution contribution) {
        var entity = mapper.toEntity(contribution);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<GoalContribution> findByGoalId(UUID goalId) {
        return jpaRepository.findByGoalIdAndActiveTrueOrderByDateDesc(goalId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}