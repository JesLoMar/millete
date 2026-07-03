package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalInvitationRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalInvitationEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoalInvitationPostgresAdapter implements GoalInvitationRepository {

    private final JpaGoalInvitationRepository jpaRepository;
    private final GoalInvitationEntityMapper mapper;

    @Override
    public GoalInvitation save(GoalInvitation invitation) {
        var entity = mapper.toEntity(invitation);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<GoalInvitation> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public Optional<GoalInvitation> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<GoalInvitation> findByGoalIdAndEmailAndStatus(UUID goalId, String email, InvitationStatus status) {
        return jpaRepository.findByGoalIdAndEmailAndStatus(goalId, email, status.name())
                .map(mapper::toDomain);
    }

    @Override
    public List<GoalInvitation> findByInvitedUserIdAndStatus(UUID invitedUserId, InvitationStatus status) {
        return jpaRepository.findByInvitedUserIdAndStatus(invitedUserId, status.name()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<GoalInvitation> findByGoalIdAndInvitedUserIdAndStatus(UUID goalId, UUID invitedUserId, InvitationStatus status) {
        return jpaRepository.findByGoalIdAndInvitedUserIdAndStatus(goalId, invitedUserId, status.name())
                .map(mapper::toDomain);
    }
}
