package com.puntomartinez.millete.dataexport.infrastructure.out.savingsgoals;

import com.puntomartinez.millete.dataexport.domain.model.SavingsGoalSnapshot;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsGoalImportAdapter")
class SavingsGoalImportAdapterTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @InjectMocks
    private SavingsGoalImportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("importSavingsGoals should create new goals with new UUIDs")
    void importSavingsGoalsShouldCreateNewGoals() {
        UUID sourceGoalId = UUID.randomUUID();
        SavingsGoalSnapshot snapshot = new SavingsGoalSnapshot(
                sourceGoalId, UUID.randomUUID(), "Vacation",
                new BigDecimal("2000.00"), new BigDecimal("500.00"),
                LocalDate.now().plusMonths(6), "HIGH", null,
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        when(savingsGoalRepository.save(any(SavingsGoal.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        int count = adapter.importSavingsGoals(List.of(snapshot), userId);

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<SavingsGoal> captor = ArgumentCaptor.forClass(SavingsGoal.class);
        verify(savingsGoalRepository).save(captor.capture());

        SavingsGoal savedGoal = captor.getValue();
        assertThat(savedGoal.getUserId()).isEqualTo(userId);
        assertThat(savedGoal.getId()).isNotEqualTo(sourceGoalId);
        assertThat(savedGoal.getName()).isEqualTo("Vacation");
        assertThat(savedGoal.getTargetAmount()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("importSavingsGoals should skip inactive goals")
    void importSavingsGoalsShouldSkipInactiveGoals() {
        SavingsGoalSnapshot inactiveSnapshot = new SavingsGoalSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), "Inactive",
                new BigDecimal("1000.00"), BigDecimal.ZERO,
                LocalDate.now().plusMonths(3), "MEDIUM", null,
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        int count = adapter.importSavingsGoals(List.of(inactiveSnapshot), userId);

        assertThat(count).isZero();
        verify(savingsGoalRepository, never()).save(any());
    }
}