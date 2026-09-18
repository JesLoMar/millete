package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalMemberEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalMemberRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GoalMemberPostgresAdapter implements GoalMemberRepository {

    private final JpaGoalMemberRepository repository;
    private final GoalMemberEntityMapper mapper;

    public GoalMemberPostgresAdapter(
            JpaGoalMemberRepository repository,
            GoalMemberEntityMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GoalMember save(GoalMember member) {
        GoalMemberEntity entity = mapper.toEntity(member);
        GoalMemberEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<GoalMember> findById(UUID id) {
        return repository.findByIdAndActiveTrue(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<GoalMember> findByGoalIdAndUserId(
            UUID goalId,
            UUID userId
    ) {
        return repository.findByGoalIdAndUserId(goalId, userId)
                .map(mapper::toDomain);
    }

    @Override
    public List<GoalMember> findActiveByGoalId(UUID goalId) {
        return repository.findByGoalIdAndActiveTrue(goalId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GoalMember> findActiveByGoalIdIn(List<UUID> goalIds) {
        if (goalIds == null || goalIds.isEmpty()) {
            return List.of();
        }
        return repository.findByGoalIdInAndActiveTrue(goalIds)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deactivateByGoalId(UUID goalId) {
        repository.deactivateByGoalId(goalId, LocalDateTime.now());
    }

    @Override
public List<GoalMember> findActiveByUserId(UUID userId) {
    return repository.findByUserIdAndActiveTrue(userId)
            .stream()
            .map(mapper::toDomain)
            .toList();
}
}