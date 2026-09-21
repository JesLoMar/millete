package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.groupgoals.domain.model.ContributionType;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers.GoalContributionEntityMapper;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.repository.JpaGoalContributionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalContributionPostgresAdapter")
class GoalContributionPostgresAdapterTest {

    @Mock
    private JpaGoalContributionRepository repository;

    @Mock
    private GoalContributionEntityMapper mapper;

    @InjectMocks
    private GoalContributionPostgresAdapter adapter;

    @Test
    @DisplayName("Should save contribution")
    void shouldSaveContribution() {
        GoalContribution domain = GoalContribution.create(
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("100.00"),
                ContributionType.DEPOSIT, LocalDateTime.now()
        );
        GoalContributionEntity entity = new GoalContributionEntity();
        GoalContributionEntity savedEntity = new GoalContributionEntity();
        GoalContribution savedDomain = domain;

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        GoalContribution result = adapter.save(domain);

        assertThat(result).isSameAs(savedDomain);
    }

    @Test
    @DisplayName("Should count by goal id and active")
    void shouldCountByGoalIdAndActive() {
        UUID goalId = UUID.randomUUID();

        when(repository.countByGoalIdAndActiveTrue(goalId)).thenReturn(10L);

        long result = adapter.countByGoalId(goalId);

        assertThat(result).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should deactivate by goal id")
    void shouldDeactivateByGoalId() {
        UUID goalId = UUID.randomUUID();

        adapter.deactivateByGoalId(goalId);

        verify(repository).deactivateByGoalId(goalId, LocalDateTime.now());
    }

    @Test
    @DisplayName("Should find by goal id with pagination")
    void shouldFindByGoalIdWithPagination() {
        UUID goalId = UUID.randomUUID();
        GoalContributionEntity entity = new GoalContributionEntity();
        GoalContribution domain = GoalContribution.create(
                goalId, UUID.randomUUID(),
                new BigDecimal("100.00"),
                ContributionType.DEPOSIT, LocalDateTime.now()
        );

        org.springframework.data.domain.Page<GoalContribution> page =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(domain)
                );

        when(repository.findByGoalIdAndActiveTrueOrderByDateDesc(
                any(UUID.class), any(PageRequest.class)
        )).thenReturn(new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(entity)
        ));
        when(mapper.toDomain(entity)).thenReturn(domain);

        var result = adapter.findByGoalId(goalId, 0, 10);

        assertThat(result.contributions()).hasSize(1);
    }

    @Test
    @DisplayName("Should sum by goal id grouped by user")
    void shouldSumByGoalIdGroupedByUser() {
        UUID goalId = UUID.randomUUID();

        when(repository.sumByGoalIdGroupedByUser(goalId))
                .thenReturn(java.util.List.of());

        var result = adapter.sumByGoalId(goalId);

        assertThat(result).isEmpty();
    }
}