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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlannedTransactionService")
class PlannedTransactionServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;

    @Mock
    private PlannedTransactionExecutionService executionService;

    @InjectMocks
    private PlannedTransactionService service;

    private PlannedTransaction existingPlannedTransaction() {
        return PlannedTransaction.create(
                USER_ID, CATEGORY_ID, new BigDecimal("100.00"),
                TransactionType.EXPENSE, "Rent",
                FrequencyType.MONTHS, 1,
                LocalDate.now().minusMonths(1), null
        );
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("Should register planned transaction")
        void shouldRegisterPlannedTransaction() {
            RegisterPlannedTransactionCommand command =
                    new RegisterPlannedTransactionCommand(
                            USER_ID, CATEGORY_ID, new BigDecimal("100.00"),
                            TransactionType.EXPENSE, "Rent",
                            FrequencyType.MONTHS, 1,
                            LocalDate.now(), null
                    );

            when(plannedTransactionRepository.save(any(PlannedTransaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            PlannedTransaction result = service.register(command);

            ArgumentCaptor<PlannedTransaction> captor =
                    ArgumentCaptor.forClass(PlannedTransaction.class);
            verify(plannedTransactionRepository).save(captor.capture());

            PlannedTransaction saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(USER_ID);
            assertThat(saved.getCategoryId()).isEqualTo(CATEGORY_ID);
            assertThat(saved.getAmount()).isEqualByComparingTo("100.00");
            assertThat(saved.getType()).isEqualTo(TransactionType.EXPENSE);
            assertThat(saved.getFrequencyType()).isEqualTo(FrequencyType.MONTHS);
            assertThat(saved.getFrequencyInterval()).isEqualTo(1);
            assertThat(result).isSameAs(saved);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update planned transaction when it exists and belongs to user")
        void shouldUpdatePlannedTransaction() {
            PlannedTransaction ptx = existingPlannedTransaction();
            UUID newCategoryId = UUID.randomUUID();
            UpdatePlannedTransactionCommand command =
                    new UpdatePlannedTransactionCommand(
                            new BigDecimal("200.00"),
                            TransactionType.INCOME,
                            "Updated",
                            FrequencyType.WEEKS,
                            2,
                            newCategoryId
                    );

            when(plannedTransactionRepository.findById(ptx.getId()))
                    .thenReturn(Optional.of(ptx));
            when(plannedTransactionRepository.save(ptx)).thenReturn(ptx);

            PlannedTransaction result = service.update(ptx.getId(), USER_ID, command);

            assertThat(result.getAmount()).isEqualByComparingTo("200.00");
            assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
            assertThat(result.getDescription()).isEqualTo("Updated");
            assertThat(result.getFrequencyType()).isEqualTo(FrequencyType.WEEKS);
            assertThat(result.getFrequencyInterval()).isEqualTo(2);
            assertThat(result.getCategoryId()).isEqualTo(newCategoryId);
            verify(plannedTransactionRepository).save(ptx);
        }

        @Test
        @DisplayName("Should throw when planned transaction does not exist")
        void shouldThrowWhenPlannedTransactionDoesNotExist() {
            UUID ptxId = UUID.randomUUID();
            UpdatePlannedTransactionCommand command =
                    new UpdatePlannedTransactionCommand(
                            new BigDecimal("200.00"),
                            TransactionType.EXPENSE,
                            "Updated",
                            FrequencyType.MONTHS, 1, null
                    );

            when(plannedTransactionRepository.findById(ptxId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(ptxId, USER_ID, command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(plannedTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when planned transaction belongs to another user")
        void shouldThrowWhenPlannedTransactionBelongsToAnotherUser() {
            UUID otherUserId = UUID.randomUUID();
            PlannedTransaction ptx = PlannedTransaction.create(
                    otherUserId, CATEGORY_ID, new BigDecimal("100.00"),
                    TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            );
            UpdatePlannedTransactionCommand command =
                    new UpdatePlannedTransactionCommand(
                            new BigDecimal("200.00"),
                            TransactionType.EXPENSE,
                            "Updated",
                            FrequencyType.MONTHS, 1, null
                    );

            when(plannedTransactionRepository.findById(ptx.getId()))
                    .thenReturn(Optional.of(ptx));

            assertThatThrownBy(() -> service.update(ptx.getId(), USER_ID, command))
                    .isInstanceOf(ForbiddenOperationException.class);

            verify(plannedTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when planned transaction is inactive")
        void shouldThrowWhenPlannedTransactionIsInactive() {
            PlannedTransaction ptx = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID,
                    new BigDecimal("100.00"), TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now(), null,
                    java.time.LocalDateTime.now(), java.time.LocalDateTime.now(),
                    false, null, 0
            );
            UpdatePlannedTransactionCommand command =
                    new UpdatePlannedTransactionCommand(
                            new BigDecimal("200.00"),
                            TransactionType.EXPENSE,
                            "Updated",
                            FrequencyType.MONTHS, 1, null
                    );

            when(plannedTransactionRepository.findById(ptx.getId()))
                    .thenReturn(Optional.of(ptx));

            assertThatThrownBy(() -> service.update(ptx.getId(), USER_ID, command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(plannedTransactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteByIdAndUserId")
    class Delete {

        @Test
        @DisplayName("Should deactivate and save planned transaction")
        void shouldDeactivateAndSave() {
            PlannedTransaction ptx = existingPlannedTransaction();

            when(plannedTransactionRepository.findById(ptx.getId()))
                    .thenReturn(Optional.of(ptx));
            when(plannedTransactionRepository.save(ptx)).thenReturn(ptx);

            service.deleteByIdAndUserId(ptx.getId(), USER_ID);

            ArgumentCaptor<PlannedTransaction> captor =
                    ArgumentCaptor.forClass(PlannedTransaction.class);
            verify(plannedTransactionRepository).save(captor.capture());

            assertThat(captor.getValue().isActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw when planned transaction does not exist")
        void shouldThrowWhenDoesNotExist() {
            UUID ptxId = UUID.randomUUID();

            when(plannedTransactionRepository.findById(ptxId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteByIdAndUserId(ptxId, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(plannedTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when planned transaction belongs to another user")
        void shouldThrowWhenBelongsToAnotherUser() {
            UUID otherUserId = UUID.randomUUID();
            PlannedTransaction ptx = PlannedTransaction.create(
                    otherUserId, CATEGORY_ID, new BigDecimal("100.00"),
                    TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            );

            when(plannedTransactionRepository.findById(ptx.getId()))
                    .thenReturn(Optional.of(ptx));

            assertThatThrownBy(() -> service.deleteByIdAndUserId(ptx.getId(), USER_ID))
                    .isInstanceOf(ForbiddenOperationException.class);

            verify(plannedTransactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getByIdAndUserId")
    class GetById {

        @Test
        @DisplayName("Should return planned transaction when found")
        void shouldReturnPlannedTransaction() {
            PlannedTransaction ptx = existingPlannedTransaction();

            when(plannedTransactionRepository.findById(ptx.getId()))
                    .thenReturn(Optional.of(ptx));

            PlannedTransaction result = service.getByIdAndUserId(ptx.getId(), USER_ID);

            assertThat(result).isSameAs(ptx);
        }

        @Test
        @DisplayName("Should throw when not found")
        void shouldThrowWhenNotFound() {
            UUID ptxId = UUID.randomUUID();

            when(plannedTransactionRepository.findById(ptxId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getByIdAndUserId(ptxId, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("queries")
    class Queries {

        @Test
        @DisplayName("Should delegate findAllByUserId to repository")
        void shouldDelegateFindAllByUserId() {
            PlannedTransaction ptx = existingPlannedTransaction();
            List<PlannedTransaction> ptxs = List.of(ptx);

            when(plannedTransactionRepository.findAllByUserId(
                    USER_ID, 0, 50, "search", TransactionType.EXPENSE
            )).thenReturn(ptxs);

            List<PlannedTransaction> result = service.findAllByUserId(
                    USER_ID, 0, 50, "search", TransactionType.EXPENSE
            );

            assertThat(result).containsExactly(ptx);
        }

        @Test
        @DisplayName("Should delegate countByUserIdAndFilters to repository")
        void shouldDelegateCountByUserIdAndFilters() {
            when(plannedTransactionRepository.countByUserIdAndFilters(
                    USER_ID, "search", TransactionType.EXPENSE
            )).thenReturn(5L);

            long result = service.countByUserIdAndFilters(
                    USER_ID, "search", TransactionType.EXPENSE
            );

            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("processScheduledTasks")
    class ProcessScheduledTasks {

        @Test
        @DisplayName("Should process all active templates in pages")
        void shouldProcessAllActiveTemplatesInPages() {
            PlannedTransaction ptx = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID,
                    new BigDecimal("100.00"), TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now().minusMonths(2), null,
                    java.time.LocalDateTime.now(), java.time.LocalDateTime.now(),
                    true, null, 0
            );

            when(plannedTransactionRepository.findAllActive(eq(0), anyInt(), any()))
                    .thenReturn(List.of(ptx));
            when(plannedTransactionRepository.findAllActive(eq(1), anyInt(), any()))
                    .thenReturn(Collections.emptyList());

            service.processScheduledTasks();

            verify(plannedTransactionRepository).findAllActive(eq(0), anyInt(), any());
            verify(plannedTransactionRepository).findAllActive(eq(1), anyInt(), any());
        }

        @Test
        @DisplayName("Should do nothing when no active templates exist")
        void shouldDoNothingWhenNoActiveTemplates() {
            when(plannedTransactionRepository.findAllActive(eq(0), anyInt(), any()))
                    .thenReturn(Collections.emptyList());

            service.processScheduledTasks();

            verify(plannedTransactionRepository).findAllActive(eq(0), anyInt(), any());
            verify(plannedTransactionRepository, never()).findAllActive(eq(1), anyInt(), any());
        }

        @Test
        @DisplayName("Should stop pagination when page returns fewer than batch size")
        void shouldStopPaginationWhenPageReturnsFewerThanBatchSize() {
            PlannedTransaction ptx = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID,
                    new BigDecimal("100.00"), TransactionType.EXPENSE, "Rent",
                    FrequencyType.MONTHS, 1,
                    LocalDate.now().minusMonths(2), null,
                    java.time.LocalDateTime.now(), java.time.LocalDateTime.now(),
                    true, null, 0
            );

            when(plannedTransactionRepository.findAllActive(eq(0), anyInt(), any()))
                    .thenReturn(List.of(ptx));

            service.processScheduledTasks();

            verify(plannedTransactionRepository).findAllActive(eq(0), anyInt(), any());
            verify(plannedTransactionRepository, never()).findAllActive(eq(1), anyInt(), any());
        }
    }
}