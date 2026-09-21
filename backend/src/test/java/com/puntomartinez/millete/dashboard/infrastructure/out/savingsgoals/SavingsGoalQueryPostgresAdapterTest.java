package com.puntomartinez.millete.dashboard.infrastructure.out.savingsgoals;

import com.puntomartinez.millete.dashboard.domain.ports.out.SavingsGoalQueryPort;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsGoalQueryPostgresAdapter")
class SavingsGoalQueryPostgresAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    private SavingsGoalQueryPostgresAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SavingsGoalQueryPostgresAdapter(savingsGoalRepository);
    }

    @Test
    @DisplayName("Should map savings goals to SavingsGoalData")
    void shouldMapSavingsGoals() {
        UUID goalId = UUID.randomUUID();
        SavingsGoal goal = SavingsGoal.reconstitute(
                goalId, USER_ID, "Vacation",
                new BigDecimal("1000.00"), new BigDecimal("500.00"),
                LocalDate.now().plusDays(30), GoalPriority.HIGH,
                null,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0),
                true
        );

        when(savingsGoalRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(goal));

        List<SavingsGoalQueryPort.SavingsGoalData> result =
                adapter.findAllByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(goalId);
        assertThat(result.get(0).name()).isEqualTo("Vacation");
        assertThat(result.get(0).targetAmount())
                .isEqualByComparingTo("1000.00");
        assertThat(result.get(0).currentAmount())
                .isEqualByComparingTo("500.00");
        assertThat(result.get(0).priority()).isEqualTo("HIGH");
        verify(savingsGoalRepository).findAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("Should return null priority when goal priority is null")
    void shouldReturnNullPriorityWhenNull() {
        SavingsGoal goal = SavingsGoal.create(
                USER_ID, "Goal", new BigDecimal("100.00"),
                LocalDate.now().plusDays(30), null, null
        );

        when(savingsGoalRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(goal));

        List<SavingsGoalQueryPort.SavingsGoalData> result =
                adapter.findAllByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).priority()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("Should return empty list when no goals")
    void shouldReturnEmptyWhenNoGoals() {
        when(savingsGoalRepository.findAllByUserId(USER_ID))
                .thenReturn(Collections.emptyList());

        List<SavingsGoalQueryPort.SavingsGoalData> result =
                adapter.findAllByUserId(USER_ID);

        assertThat(result).isEmpty();
    }
}