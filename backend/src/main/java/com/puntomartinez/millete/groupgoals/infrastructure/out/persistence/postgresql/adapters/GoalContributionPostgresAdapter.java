package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.ContributionType;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetContributionHistoryUseCase.PaginatedContributions;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalContributionEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalContributionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GoalContributionPostgresAdapter
        implements GoalContributionRepository {

    private final JpaGoalContributionRepository repository;
    private final GoalContributionEntityMapper mapper;
    private final TimeProvider timeProvider;

    public GoalContributionPostgresAdapter(
            JpaGoalContributionRepository repository,
            GoalContributionEntityMapper mapper,
            TimeProvider timeProvider
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.timeProvider = timeProvider;
    }

    @Override
    public GoalContribution save(GoalContribution contribution) {
        GoalContributionEntity entity = mapper.toEntity(contribution);
        GoalContributionEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public PaginatedContributions findByGoalId(
            UUID goalId,
            int page,
            int size
    ) {
        Page<GoalContribution> result = repository
                .findByGoalIdAndActiveTrueOrderByDateDesc(
                        goalId,
                        PageRequest.of(page, size)
                )
                .map(mapper::toDomain);

        return new PaginatedContributions(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements(),
                result.getSize(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Override
    public long countByGoalId(UUID goalId) {
        return repository.countByGoalIdAndActiveTrue(goalId);
    }

    @Override
    public List<MemberContributionTotals> sumByGoalId(UUID goalId) {
        return repository.sumByGoalIdGroupedByUser(goalId)
                .stream()
                .map(projection -> new MemberContributionTotals(
                        projection.getUserId(),
                        projection.getTotalDeposits(),
                        projection.getTotalWithdrawals(),
                        projection.getTotalDeposits()
                                .subtract(projection.getTotalWithdrawals())
                ))
                .toList();
    }

    @Override
    public void deactivateByGoalId(UUID goalId) {
        repository.deactivateByGoalId(goalId, timeProvider.instantNow());
    }
}