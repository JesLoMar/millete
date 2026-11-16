package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalMemberEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalMemberRepository;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalMemberPostgresAdapterTest {
    @Mock
    private JpaGoalMemberRepository jpaRepository;
    @Mock
    private GoalMemberEntityMapper mapper;
    @Mock
    private TimeProvider timeProvider;
    @InjectMocks
    private GoalMemberPostgresAdapter adapter;

    @Test
    void findsLockedActiveMemberAndMapsItToDomain() {
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GoalMemberEntity entity = new GoalMemberEntity();
        GoalMember member = mock(GoalMember.class);
        when(jpaRepository.findByGoalIdAndUserIdAndActiveTrue(goalId, userId))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(member);

        Optional<GoalMember> result =
                adapter.findActiveByGoalIdAndUserIdForUpdate(goalId, userId);

        assertThat(result).containsSame(member);
        verify(jpaRepository).findByGoalIdAndUserIdAndActiveTrue(goalId, userId);
        verify(mapper).toDomain(entity);
    }

    @Test
    void returnsEmptyWhenNoActiveMemberCanBeLocked() {
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(jpaRepository.findByGoalIdAndUserIdAndActiveTrue(goalId, userId))
                .thenReturn(Optional.empty());

        assertThat(adapter.findActiveByGoalIdAndUserIdForUpdate(goalId, userId))
                .isEmpty();
        verifyNoInteractions(mapper);
    }
}
