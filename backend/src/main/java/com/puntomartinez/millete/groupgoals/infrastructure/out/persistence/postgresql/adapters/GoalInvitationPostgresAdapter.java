package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalInvitationRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalInvitationEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalInvitationRepository;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GoalInvitationPostgresAdapter
        implements GoalInvitationRepository {

    private final JpaGoalInvitationRepository repository;
    private final GoalInvitationEntityMapper mapper;
    private final TimeProvider timeProvider;

    public GoalInvitationPostgresAdapter(
            JpaGoalInvitationRepository repository,
            GoalInvitationEntityMapper mapper,
            TimeProvider timeProvider
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.timeProvider = timeProvider;
    }

    @Override
    public GoalInvitation save(GoalInvitation invitation) {
        GoalInvitationEntity entity = mapper.toEntity(invitation);
        GoalInvitationEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<GoalInvitation> findById(UUID id) {
        return repository.findByIdAndActiveTrue(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<GoalInvitation> findByGoalIdAndInvitedUserIdAndStatus(
            UUID goalId,
            UUID invitedUserId,
            InvitationStatus status
    ) {
        return repository
                .findByGoalIdAndInvitedUserIdAndStatusAndActiveTrue(
                        goalId,
                        invitedUserId,
                        status.name()
                )
                .map(mapper::toDomain);
    }

    @Override
    public List<GoalInvitation> findActiveAndNotExpiredByInvitedUserIdAndStatus(
            UUID invitedUserId,
            InvitationStatus status
    ) {
        return repository
                .findActiveAndNotExpiredByInvitedUserIdAndStatus(
                        invitedUserId,
                        status.name(),
                        timeProvider.instantNow()
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GoalInvitation> findActiveByGoalIdAndStatus(
            UUID goalId,
            InvitationStatus status
    ) {
        return repository
                .findByGoalIdAndStatusAndActiveTrue(
                        goalId,
                        status.name()
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deactivatePendingByGoalId(UUID goalId) {
        repository.deactivatePendingByGoalId(goalId, timeProvider.instantNow());
    }
}