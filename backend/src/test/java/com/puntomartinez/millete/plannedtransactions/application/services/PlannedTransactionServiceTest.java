package com.puntomartinez.millete.plannedtransactions.application.services;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase.RegisterPlannedTransactionCommand;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase.UpdatePlannedTransactionCommand;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlannedTransactionService")
class PlannedTransactionServiceTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;

    @Mock
    private PlannedTransactionExecutionService executionService;

    @InjectMocks
    private PlannedTransactionService service;

    private PlannedTransaction buildTemplate(UUID userId) {
        return PlannedTransaction.reconstitute(
                UUID.randomUUID(), userId, CATEGORY_ID,
                new BigDecimal("100.00"), TransactionType.EXPENSE,
                "Rent", FrequencyType.MONTHS, 1,
                LocalDate.now().minusMonths(1), null,
                LocalDateTime.now(), LocalDateTime.now(),
                true, null, 0
        );
    }

    @Nested
    @DisplayName("register")
    class Register {
        @Test
        @DisplayName("Should create and save planned transaction from command")
        void shouldRegister() {
            RegisterPlannedTransactionCommand command = new RegisterPlannedTransactionCommand(
                    USER_ID, CATEGORY_ID, new BigDecimal("100.00"),
                    TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            );
            when(plannedTransactionRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            PlannedTransaction result = service.register(command);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            ArgumentCaptor<PlannedTransaction> captor =
                    ArgumentCaptor.forClass(PlannedTransaction.class);
            verify(plannedTransactionRepository).save(captor.capture());
            assertThat(captor.getValue().getDescription()).isEqualTo("Rent");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("Should update and save planned transaction for own user")
        void shouldUpdate() {
            PlannedTransaction template = buildTemplate(USER_ID);
            UpdatePlannedTransactionCommand command = new UpdatePlannedTransactionCommand(
                    new BigDecimal("200.00"), TransactionType.INCOME,
                    "Updated", FrequencyType.WEEKS, 2, null
            );
            when(plannedTransactionRepository.findById(template.getId()))
                    .thenReturn(Optional.of(template));
            when(plannedTransactionRepository.save(template)).thenReturn(template);

            PlannedTransaction result =
                    service.update(template.getId(), USER_ID, command);

            assertThat(result.getAmount()).isEqualByComparingTo("200.00");
            verify(plannedTransactionRepository).save(template);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(plannedTransactionRepository.findById(id))
                    .thenReturn(Optional.empty());
            UpdatePlannedTransactionCommand command = new UpdatePlannedTransactionCommand(
                    BigDecimal.TEN, TransactionType.EXPENSE, "Desc",
                    FrequencyType.MONTHS, 1, null
            );
            assertThatThrownBy(() -> service.update(id, USER_ID, command))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw ForbiddenOperationException when user mismatch")
        void shouldThrowWhenUserMismatch() {
            PlannedTransaction template = buildTemplate(OTHER_USER_ID);
            when(plannedTransactionRepository.findById(template.getId()))
                    .thenReturn(Optional.of(template));
            UpdatePlannedTransactionCommand command = new UpdatePlannedTransactionCommand(
                    BigDecimal.TEN, TransactionType.EXPENSE, "Desc",
                    FrequencyType.MONTHS, 1, null
            );
            assertThatThrownBy(() ->
                    service.update(template.getId(), USER_ID, command)
            ).isInstanceOf(ForbiddenOperationException.class);
        }

        @Test
        @DisplayName("Should throw when template is inactive")
        void shouldThrowWhenInactive() {
            PlannedTransaction template = buildTemplate(USER_ID);
            template.deactivate();
            when(plannedTransactionRepository.findById(template.getId()))
                    .thenReturn(Optional.of(template));
            UpdatePlannedTransactionCommand command = new UpdatePlannedTransactionCommand(
                    BigDecimal.TEN, TransactionType.EXPENSE, "Desc",
                    FrequencyType.MONTHS, 1, null
            );
            assertThatThrownBy(() ->
                    service.update(template.getId(), USER_ID, command)
            ).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteByIdAndUserId")
    class Delete {
        @Test
        @DisplayName("Should deactivate and save template for own user")
        void shouldDeactivate() {
            PlannedTransaction template = buildTemplate(USER_ID);
            when(plannedTransactionRepository.findById(template.getId()))
                    .thenReturn(Optional.of(template));
            when(plannedTransactionRepository.save(template)).thenReturn(template);

            service.deleteByIdAndUserId(template.getId(), USER_ID);

            assertThat(template.isActive()).isFalse();
            verify(plannedTransactionRepository).save(template);
        }

        @Test
        @DisplayName("Should throw when template not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(plannedTransactionRepository.findById(id))
                    .thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.deleteByIdAndUserId(id, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("queries")
    class Queries {
        @Test
        @DisplayName("Should delegate findAllByUserId with filters")
        void shouldDelegateFindAllByUserId() {
            PlannedTransaction template = buildTemplate(USER_ID);
            when(plannedTransactionRepository.findAllByUserId(
                    eq(USER_ID), eq(0), eq(10), eq("rent"), eq(TransactionType.EXPENSE)
            )).thenReturn(List.of(template));

            List<PlannedTransaction> result = service.findAllByUserId(
                    USER_ID, 0, 10, "rent", TransactionType.EXPENSE
            );
            assertThat(result).hasSize(1);
            verify(plannedTransactionRepository).findAllByUserId(
                    USER_ID, 0, 10, "rent", TransactionType.EXPENSE
            );
        }

        @Test
        @DisplayName("Should delegate countByUserIdAndFilters")
        void shouldDelegateCount() {
            when(plannedTransactionRepository.countByUserIdAndFilters(
                    USER_ID, "rent", TransactionType.EXPENSE
            )).thenReturn(5L);

            long result = service.countByUserIdAndFilters(
                    USER_ID, "rent", TransactionType.EXPENSE
            );
            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should return template when getByIdAndUserId finds it")
        void shouldReturnTemplate() {
            PlannedTransaction template = buildTemplate(USER_ID);
            when(plannedTransactionRepository.findById(template.getId()))
                    .thenReturn(Optional.of(template));

            PlannedTransaction result =
                    service.getByIdAndUserId(template.getId(), USER_ID);
            assertThat(result).isSameAs(template);
        }

        @Test
        @DisplayName("Should throw when getByIdAndUserId not found")
        void shouldThrowWhenGetByIdNotFound() {
            UUID id = UUID.randomUUID();
            when(plannedTransactionRepository.findById(id))
                    .thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.getByIdAndUserId(id, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("processScheduledTasks")
    class ProcessScheduledTasks {
        @Test
        @DisplayName("Should process pending templates in batches and stop when empty")
        void shouldProcessInBatches() {
            LocalDate start = LocalDate.now().minusDays(5);
            PlannedTransaction pending = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID,
                    new BigDecimal("50.00"), TransactionType.EXPENSE,
                    "Daily", FrequencyType.DAYS, 1,
                    start, null,
                    LocalDateTime.now(), LocalDateTime.now(),
                    true, null, 0
            );

            doAnswer(invocation -> {
                PlannedTransaction template = invocation.getArgument(0);
                LocalDate date = invocation.getArgument(1);
                template.markAsExecuted(date);
                return null;
            }).when(executionService).execute(any(PlannedTransaction.class), any(LocalDate.class));

            when(plannedTransactionRepository.findAllActive(eq(0), anyInt(), any()))
                    .thenReturn(List.of(pending));

            service.processScheduledTasks();

            verify(executionService, atLeastOnce())
                    .execute(eq(pending), any(LocalDate.class));
        }

        @Test
        @DisplayName("Should not invoke execution when no active templates")
        void shouldNotInvokeWhenNoActiveTemplates() {
            when(plannedTransactionRepository.findAllActive(eq(0), anyInt(), any()))
                    .thenReturn(Collections.emptyList());

            service.processScheduledTasks();

            verify(executionService, never()).execute(any(), any());
        }

        @Test
        @DisplayName("Should deactivate template after MAX_CONSECUTIVE_FAILURES")
        void shouldDeactivateAfterMaxFailures() {
            LocalDate start = LocalDate.now().minusDays(5);
            PlannedTransaction failingTemplate = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID,
                    new BigDecimal("50.00"), TransactionType.EXPENSE,
                    "Fail", FrequencyType.DAYS, 1,
                    start, null,
                    LocalDateTime.now(), LocalDateTime.now(),
                    true, null, PlannedTransaction.MAX_CONSECUTIVE_FAILURES - 1
            );

            when(plannedTransactionRepository.findAllActive(eq(0), anyInt(), any()))
                    .thenReturn(List.of(failingTemplate));

            doThrow(new RuntimeException("fail"))
                    .when(executionService).execute(eq(failingTemplate), any(LocalDate.class));

            service.processScheduledTasks();

            assertThat(failingTemplate.isActive()).isFalse();
            verify(plannedTransactionRepository, atLeastOnce()).save(failingTemplate);
        }

        @Test
        @DisplayName("Should increment failure count without deactivating below max")
        void shouldIncrementFailureCountBelowMax() {
            LocalDate start = LocalDate.now().minusDays(5);
            PlannedTransaction failingTemplate = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID,
                    new BigDecimal("50.00"), TransactionType.EXPENSE,
                    "Fail", FrequencyType.DAYS, 1,
                    start, null,
                    LocalDateTime.now(), LocalDateTime.now(),
                    true, null, 0
            );

            when(plannedTransactionRepository.findAllActive(eq(0), anyInt(), any()))
                    .thenReturn(List.of(failingTemplate));

            doThrow(new RuntimeException("fail"))
                    .when(executionService).execute(eq(failingTemplate), any(LocalDate.class));

            service.processScheduledTasks();

            assertThat(failingTemplate.getFailureCount()).isEqualTo(1);
            assertThat(failingTemplate.isActive()).isTrue();
        }
    }
}