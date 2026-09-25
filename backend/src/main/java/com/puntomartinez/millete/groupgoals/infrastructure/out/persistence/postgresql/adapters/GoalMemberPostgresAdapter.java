package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalMemberEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalMemberRepository;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoalMemberPostgresAdapter implements GoalMemberRepository {

    private final JpaGoalMemberRepository jpaRepository;
    private final GoalMemberEntityMapper mapper;
    private final TimeProvider timeProvider;

    @Override
    public GoalMember save(GoalMember goalMember) {
        GoalMemberEntity entity = mapper.toEntity(goalMember);

        GoalMemberEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<GoalMember> findByGoalIdAndUserId(
            UUID goalId,
            UUID userId
    ) {
        return jpaRepository
                .findByGoalIdAndUserId(goalId, userId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<GoalMember> findById(UUID id) {
        return jpaRepository
                .findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<GoalMember> findActiveByUserId(UUID userId) {
        return jpaRepository
                .findByUserIdAndActiveTrue(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GoalMember> findActiveByGoalId(UUID goalId) {
        return jpaRepository
                .findByGoalIdAndActiveTrue(goalId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GoalMember> findActiveByGoalIdIn(List<UUID> goalIds) {
        if (goalIds == null || goalIds.isEmpty()) {
            return List.of();
        }

        return jpaRepository
                .findByGoalIdInAndActiveTrue(goalIds)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deactivateByGoalId(UUID goalId) {
        jpaRepository.deactivateByGoalId(
                goalId,
                timeProvider.now()
        );
    }

    @Override
    public void deleteByGoalIdAndUserId(
            UUID goalId,
            UUID userId
    ) {
        jpaRepository.deleteByGoalIdAndUserId(
                goalId,
                userId
        );
    }
}
