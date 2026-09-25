package com.puntomartinez.millete.savingsgoals.application.services;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.AddContributionToGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.CreateSavingsGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.UpdateSavingsGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.WithdrawFromGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import java.time.Instant;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsGoalService")
class SavingsGoalServiceTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private SavingsGoal existingGoal() {
        return SavingsGoal.create(TIME, 
                USER_ID,
                "Vacation",
                new BigDecimal("1000.00"),
                LocalDate.now().plusDays(30),
                GoalPriority.MEDIUM,
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create and save savings goal")
        void shouldCreateAndSaveSavingsGoal() {
            CreateSavingsGoalCommand command = new CreateSavingsGoalCommand(
                    USER_ID,
                    "Car",
                    new BigDecimal("5000.00"),
                    LocalDate.now().plusDays(90),
                    GoalPriority.HIGH,
                    "https://example.com"
            );

            when(savingsGoalRepository.save(any(SavingsGoal.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            SavingsGoal result = savingsGoalService.create(command);

            ArgumentCaptor<SavingsGoal> captor = ArgumentCaptor.forClass(SavingsGoal.class);
            verify(savingsGoalRepository).save(captor.capture());

            SavingsGoal saved = captor.getValue();

            assertThat(saved.getUserId()).isEqualTo(USER_ID);
            assertThat(saved.getName()).isEqualTo("Car");
            assertThat(saved.getTargetAmount()).isEqualByComparingTo("5000.00");
            assertThat(saved.getPriority()).isEqualTo(GoalPriority.HIGH);
            assertThat(saved.getLink()).isEqualTo("https://example.com");
            assertThat(saved.isActive()).isTrue();
            assertThat(result).isSameAs(saved);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update savings goal when it exists")
        void shouldUpdateSavingsGoalWhenItExists() {
            SavingsGoal goal = existingGoal();
            UpdateSavingsGoalCommand command = new UpdateSavingsGoalCommand(
                    goal.getId(),
                    USER_ID,
                    "Updated",
                    new BigDecimal("2000.00"),
                    null,
                    GoalPriority.HIGH,
                    null
            );

            when(savingsGoalRepository.findByIdAndUserId(goal.getId(), USER_ID))
                    .thenReturn(Optional.of(goal));
            when(savingsGoalRepository.save(goal)).thenReturn(goal);

            SavingsGoal result = savingsGoalService.update(command);

            assertThat(result.getName()).isEqualTo("Updated");
            assertThat(result.getTargetAmount()).isEqualByComparingTo("2000.00");
            assertThat(result.getPriority()).isEqualTo(GoalPriority.HIGH);
            verify(savingsGoalRepository).save(goal);
        }

        @Test
        @DisplayName("Should throw when savings goal does not exist")
        void shouldThrowWhenSavingsGoalDoesNotExist() {
            UUID goalId = UUID.randomUUID();
            UpdateSavingsGoalCommand command = new UpdateSavingsGoalCommand(
                    goalId,
                    USER_ID,
                    "Updated",
                    null,
                    null,
                    null,
                    null
            );

            when(savingsGoalRepository.findByIdAndUserId(goalId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> savingsGoalService.update(command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(savingsGoalRepository, never()).save(any(SavingsGoal.class));
        }
    }

    @Nested
    @DisplayName("addContribution")
    class AddContribution {

        @Test
        @DisplayName("Should add contribution when savings goal exists")
        void shouldAddContributionWhenSavingsGoalExists() {
            SavingsGoal goal = existingGoal();
            AddContributionToGoalCommand command =
                    new AddContributionToGoalCommand(goal.getId(), USER_ID, new BigDecimal("100.00"));

            when(savingsGoalRepository.findByIdAndUserId(goal.getId(), USER_ID))
                    .thenReturn(Optional.of(goal));
            when(savingsGoalRepository.save(goal)).thenReturn(goal);

            SavingsGoal result = savingsGoalService.addContribution(command);

            assertThat(result.getCurrentAmount()).isEqualByComparingTo("100.00");
            verify(savingsGoalRepository).save(goal);
        }

        @Test
        @DisplayName("Should throw when savings goal does not exist")
        void shouldThrowWhenSavingsGoalDoesNotExist() {
            UUID goalId = UUID.randomUUID();
            AddContributionToGoalCommand command =
                    new AddContributionToGoalCommand(goalId, USER_ID, BigDecimal.TEN);

            when(savingsGoalRepository.findByIdAndUserId(goalId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> savingsGoalService.addContribution(command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(savingsGoalRepository, never()).save(any(SavingsGoal.class));
        }
    }

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("Should withdraw amount when savings goal exists and has enough balance")
        void shouldWithdrawAmountWhenSavingsGoalExists() {
            SavingsGoal goal = existingGoal();
            goal.addContribution(new BigDecimal("100.00"));

            WithdrawFromGoalCommand command =
                    new WithdrawFromGoalCommand(goal.getId(), USER_ID, new BigDecimal("30.00"));

            when(savingsGoalRepository.findByIdAndUserId(goal.getId(), USER_ID))
                    .thenReturn(Optional.of(goal));
            when(savingsGoalRepository.save(goal)).thenReturn(goal);

            SavingsGoal result = savingsGoalService.withdraw(command);

            assertThat(result.getCurrentAmount()).isEqualByComparingTo("70.00");
            verify(savingsGoalRepository).save(goal);
        }

        @Test
        @DisplayName("Should throw when savings goal does not exist")
        void shouldThrowWhenSavingsGoalDoesNotExist() {
            UUID goalId = UUID.randomUUID();
            WithdrawFromGoalCommand command =
                    new WithdrawFromGoalCommand(goalId, USER_ID, BigDecimal.TEN);

            when(savingsGoalRepository.findByIdAndUserId(goalId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> savingsGoalService.withdraw(command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(savingsGoalRepository, never()).save(any(SavingsGoal.class));
        }
    }

    @Nested
    @DisplayName("queries")
    class Queries {

        @Test
        @DisplayName("Should delegate findByUserId to repository")
        void shouldDelegateFindByUserIdToRepository() {
            SavingsGoal goal = existingGoal();
            List<SavingsGoal> goals = List.of(goal);

            when(savingsGoalRepository.findAllByUserId(USER_ID)).thenReturn(goals);

            List<SavingsGoal> result = savingsGoalService.findByUserId(USER_ID);

            assertThat(result).containsExactly(goal);
        }

        @Test
        @DisplayName("Should delegate paginated findByUserId to repository")
        void shouldDelegatePaginatedFindByUserIdToRepository() {
            SavingsGoal goal = existingGoal();
            List<SavingsGoal> goals = List.of(goal);

            when(savingsGoalRepository.findAllByUserId(USER_ID, 0, 10, "search"))
                    .thenReturn(goals);

            List<SavingsGoal> result = savingsGoalService.findByUserId(USER_ID, 0, 10, "search");

            assertThat(result).containsExactly(goal);
        }

        @Test
        @DisplayName("Should delegate countByUserIdAndFilters to repository")
        void shouldDelegateCountByUserIdAndFiltersToRepository() {
            when(savingsGoalRepository.countByUserIdAndFilters(USER_ID, "search"))
                    .thenReturn(5L);

            long result = savingsGoalService.countByUserIdAndFilters(USER_ID, "search");

            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should return goal when getByIdAndUserId finds it")
        void shouldReturnGoalWhenGetByIdAndUserIdFindsIt() {
            SavingsGoal goal = existingGoal();

            when(savingsGoalRepository.findByIdAndUserId(goal.getId(), USER_ID))
                    .thenReturn(Optional.of(goal));

            SavingsGoal result = savingsGoalService.getByIdAndUserId(goal.getId(), USER_ID);

            assertThat(result).isSameAs(goal);
        }

        @Test
        @DisplayName("Should throw when getByIdAndUserId does not find goal")
        void shouldThrowWhenGetByIdAndUserIdDoesNotFindGoal() {
            UUID goalId = UUID.randomUUID();

            when(savingsGoalRepository.findByIdAndUserId(goalId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> savingsGoalService.getByIdAndUserId(goalId, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteByIdAndUserId")
    class Delete {

        @Test
        @DisplayName("Should deactivate and save savings goal")
        void shouldDeactivateAndSaveSavingsGoal() {
            SavingsGoal goal = existingGoal();

            when(savingsGoalRepository.findByIdAndUserId(goal.getId(), USER_ID))
                    .thenReturn(Optional.of(goal));

            savingsGoalService.deleteByIdAndUserId(goal.getId(), USER_ID);

            ArgumentCaptor<SavingsGoal> captor = ArgumentCaptor.forClass(SavingsGoal.class);
            verify(savingsGoalRepository).save(captor.capture());

            assertThat(captor.getValue().isActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw when savings goal does not exist")
        void shouldThrowWhenSavingsGoalDoesNotExist() {
            UUID goalId = UUID.randomUUID();

            when(savingsGoalRepository.findByIdAndUserId(goalId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> savingsGoalService.deleteByIdAndUserId(goalId, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(savingsGoalRepository, never()).save(any(SavingsGoal.class));
        }
    }
}