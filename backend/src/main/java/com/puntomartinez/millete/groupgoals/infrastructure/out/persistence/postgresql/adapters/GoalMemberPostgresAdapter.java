package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalMemberEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoalMemberPostgresAdapter implements GoalMemberRepository {

    private final JpaGoalMemberRepository jpaRepository;
    private final GoalMemberEntityMapper mapper;

    @Override
    public GoalMember save(GoalMember goalMember) {
        var entity = mapper.toEntity(goalMember);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<GoalMember> findByGoalIdAndUserId(UUID goalId, UUID userId) {
        return jpaRepository.findByGoalIdAndUserId(goalId, userId).map(mapper::toDomain);
    }

    @Override
    public Optional<GoalMember> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<GoalMember> findByGoalId(UUID goalId) {
        return jpaRepository.findByGoalIdAndActiveTrue(goalId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteByGoalIdAndUserId(UUID goalId, UUID userId) {
        jpaRepository.deleteByGoalIdAndUserId(goalId, userId);
    }

    @Override
    public List<GoalMember> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}