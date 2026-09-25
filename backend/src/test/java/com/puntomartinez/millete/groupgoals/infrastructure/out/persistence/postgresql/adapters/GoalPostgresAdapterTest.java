package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalUnitEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalUnitEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalUnitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalPostgresAdapter")

class GoalPostgresAdapterTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    private static final TimeProvider TIME =
            new FixedTimeProvider(Instant.parse("2024-01-01T10:00:00Z"));


    @Mock
    private JpaGoalUnitRepository jpaRepository;

    @Mock
    private GoalUnitEntityMapper mapper;

    @InjectMocks
    private GoalPostgresAdapter adapter;

    @Test
    @DisplayName("Should save goal unit")
    void shouldSaveGoalUnit() {
        GoalUnit domain = GoalUnit.create(TIME, 
                "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE
        );
        GoalUnitEntity entity = new GoalUnitEntity();
        GoalUnitEntity savedEntity = new GoalUnitEntity();
        GoalUnit savedDomain = domain;

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        GoalUnit result = adapter.save(domain);

        assertThat(result).isSameAs(savedDomain);
        verify(mapper).toEntity(domain);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("Should find by id")
    void shouldFindById() {
        UUID goalId = UUID.randomUUID();
        GoalUnitEntity entity = new GoalUnitEntity();
        GoalUnit domain = GoalUnit.create(TIME, 
                "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE
        );

        when(jpaRepository.findById(goalId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<GoalUnit> result = adapter.findById(goalId);

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
        UUID goalId = UUID.randomUUID();

        when(jpaRepository.findById(goalId)).thenReturn(Optional.empty());

        Optional<GoalUnit> result = adapter.findById(goalId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find by ids")
    void shouldFindByIds() {
        UUID goalId = UUID.randomUUID();
        GoalUnitEntity entity = new GoalUnitEntity();
        GoalUnit domain = GoalUnit.create(TIME, 
                "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE
        );

        when(jpaRepository.findAllById(List.of(goalId)))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<GoalUnit> result = adapter.findByIds(List.of(goalId));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should count by user id")
    void shouldCountByUserId() {
        UUID userId = UUID.randomUUID();

        when(jpaRepository.countActiveByUserId(userId)).thenReturn(5L);

        long result = adapter.countByUserId(userId);

        assertThat(result).isEqualTo(5L);
    }
}