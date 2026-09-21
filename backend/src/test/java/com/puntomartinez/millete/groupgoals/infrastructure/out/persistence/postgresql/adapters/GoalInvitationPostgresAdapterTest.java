package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalInvitationEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalInvitationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalInvitationPostgresAdapter")
class GoalInvitationPostgresAdapterTest {

    @Mock
    private JpaGoalInvitationRepository repository;

    @Mock
    private GoalInvitationEntityMapper mapper;

    @InjectMocks
    private GoalInvitationPostgresAdapter adapter;

    @Test
    @DisplayName("Should save invitation")
    void shouldSaveInvitation() {
        GoalInvitation domain = GoalInvitation.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        GoalInvitationEntity entity = new GoalInvitationEntity();
        GoalInvitationEntity savedEntity = new GoalInvitationEntity();
        GoalInvitation savedDomain = domain;

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        GoalInvitation result = adapter.save(domain);

        assertThat(result).isSameAs(savedDomain);
    }

    @Test
    @DisplayName("Should find by id and active")
    void shouldFindByIdAndActive() {
        UUID invitationId = UUID.randomUUID();
        GoalInvitationEntity entity = new GoalInvitationEntity();
        GoalInvitation domain = GoalInvitation.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        when(repository.findByIdAndActiveTrue(invitationId))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<GoalInvitation> result = adapter.findById(invitationId);

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should find by goal id and invited user id and status")
    void shouldFindByGoalIdAndInvitedUserIdAndStatus() {
        UUID goalId = UUID.randomUUID();
        UUID invitedId = UUID.randomUUID();
        GoalInvitationEntity entity = new GoalInvitationEntity();
        GoalInvitation domain = GoalInvitation.create(
                goalId, UUID.randomUUID(), invitedId
        );

        when(repository.findByGoalIdAndInvitedUserIdAndStatusAndActiveTrue(
                goalId, invitedId, InvitationStatus.PENDING.name()
        )).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<GoalInvitation> result =
                adapter.findByGoalIdAndInvitedUserIdAndStatus(
                        goalId, invitedId, InvitationStatus.PENDING
                );

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should find active and not expired by invited user id and status")
    void shouldFindActiveAndNotExpiredByInvitedUserIdAndStatus() {
        UUID invitedId = UUID.randomUUID();
        GoalInvitationEntity entity = new GoalInvitationEntity();
        GoalInvitation domain = GoalInvitation.create(
                UUID.randomUUID(), UUID.randomUUID(), invitedId
        );

        when(repository.findActiveAndNotExpiredByInvitedUserIdAndStatus(
                invitedId, InvitationStatus.PENDING.name(), LocalDateTime.now()
        )).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<GoalInvitation> result =
                adapter.findActiveAndNotExpiredByInvitedUserIdAndStatus(
                        invitedId, InvitationStatus.PENDING
                );

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should find active by goal id and status")
    void shouldFindActiveByGoalIdAndStatus() {
        UUID goalId = UUID.randomUUID();
        GoalInvitationEntity entity = new GoalInvitationEntity();
        GoalInvitation domain = GoalInvitation.create(
                goalId, UUID.randomUUID(), UUID.randomUUID()
        );

        when(repository.findByGoalIdAndStatusAndActiveTrue(
                goalId, InvitationStatus.PENDING.name()
        )).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<GoalInvitation> result =
                adapter.findActiveByGoalIdAndStatus(
                        goalId, InvitationStatus.PENDING
                );

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should deactivate pending by goal id")
    void shouldDeactivatePendingByGoalId() {
        UUID goalId = UUID.randomUUID();

        adapter.deactivatePendingByGoalId(goalId);

        verify(repository).deactivatePendingByGoalId(
                eq(goalId),
                any(LocalDateTime.class)
        );
    }
}