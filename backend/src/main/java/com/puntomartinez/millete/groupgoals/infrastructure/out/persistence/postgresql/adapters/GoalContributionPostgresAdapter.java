package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalContributionEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalContributionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoalContributionPostgresAdapter
        implements GoalContributionRepository {

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
        return jpaRepository
                .findByGoalIdAndActiveTrueOrderByDateDesc(goalId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GoalContribution> findByGoalId(
            UUID goalId,
            int page,
            int size) {

        return jpaRepository
                .findByGoalIdAndActiveTrueOrderByDateDesc(
                        goalId,
                        PageRequest.of(page, size)
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByGoalId(UUID goalId) {
        return jpaRepository.countByGoalIdAndActiveTrue(goalId);
    }

    @Override
    public Map<UUID, BigDecimal> sumByUserId(UUID goalId) {
        return jpaRepository
                .sumByUserId(goalId)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }
}