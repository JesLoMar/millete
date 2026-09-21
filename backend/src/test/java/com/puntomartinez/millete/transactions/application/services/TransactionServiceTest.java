package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase.RegisterTransactionResult;
import com.puntomartinez.millete.transactions.domain.ports.in.UpdateTransactionUseCase.UpdateTransactionCommand;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository.TransactionAggregates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService")
class TransactionServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction existingTransaction() {
        return Transaction.create(
                USER_ID,
                CATEGORY_ID,
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                TransactionType.EXPENSE,
                "Original"
        );
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("Should register transaction when category is null")
        void shouldRegisterTransactionWhenCategoryIsNull() {
            RegisterTransactionCommand command = new RegisterTransactionCommand(
                    USER_ID,
                    null,
                    new BigDecimal("100.00"),
                    LocalDateTime.now(),
                    TransactionType.INCOME,
                    "Salary"
            );

            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RegisterTransactionResult result = transactionService.register(command);

            assertThat(result.transaction()).isNotNull();
            assertThat(result.transaction().getUserId()).isEqualTo(USER_ID);
            assertThat(result.limitExceeded()).isFalse();
        }

        @Test
        @DisplayName("Should register transaction when category exists and belongs to user")
        void shouldRegisterTransactionWhenCategoryExists() {
            RegisterTransactionCommand command = new RegisterTransactionCommand(
                    USER_ID,
                    CATEGORY_ID,
                    new BigDecimal("100.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Groceries"
            );

            Category category = Category.create(USER_ID, "Food", "#FF0000", null);

            when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                    .thenReturn(Optional.of(category));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(new TransactionAggregates(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0L
            ));

            RegisterTransactionResult result = transactionService.register(command);

            assertThat(result.transaction()).isNotNull();
            assertThat(result.limitExceeded()).isFalse();
        }

        @Test
        @DisplayName("Should throw when category does not belong to user")
        void shouldThrowWhenCategoryDoesNotBelongToUser() {
            RegisterTransactionCommand command = new RegisterTransactionCommand(
                    USER_ID,
                    CATEGORY_ID,
                    new BigDecimal("100.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Groceries"
            );

            when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.register(command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should flag limit exceeded when expense exceeds 70% of income")
        void shouldFlagLimitExceededWhenExpenseExceeds70Percent() {
            RegisterTransactionCommand command = new RegisterTransactionCommand(
                    USER_ID,
                    null,
                    new BigDecimal("100.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Expense"
            );

            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(new TransactionAggregates(
                    new BigDecimal("1000.00"),
                    new BigDecimal("800.00"),
                    10L
            ));

            RegisterTransactionResult result = transactionService.register(command);

            assertThat(result.limitExceeded()).isTrue();
        }

        @Test
        @DisplayName("Should not flag limit exceeded when expense is below 70% of income")
        void shouldNotFlagLimitExceededWhenExpenseBelow70Percent() {
            RegisterTransactionCommand command = new RegisterTransactionCommand(
                    USER_ID,
                    null,
                    new BigDecimal("100.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Expense"
            );

            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(new TransactionAggregates(
                    new BigDecimal("1000.00"),
                    new BigDecimal("500.00"),
                    10L
            ));

            RegisterTransactionResult result = transactionService.register(command);

            assertThat(result.limitExceeded()).isFalse();
        }

        @Test
        @DisplayName("Should not flag limit exceeded for INCOME transactions")
        void shouldNotFlagLimitExceededForIncome() {
            RegisterTransactionCommand command = new RegisterTransactionCommand(
                    USER_ID,
                    null,
                    new BigDecimal("100.00"),
                    LocalDateTime.now(),
                    TransactionType.INCOME,
                    "Salary"
            );

            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RegisterTransactionResult result = transactionService.register(command);

            assertThat(result.limitExceeded()).isFalse();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update transaction when it exists and category is valid")
        void shouldUpdateTransactionWhenItExists() {
            Transaction transaction = existingTransaction();
            UpdateTransactionCommand command = new UpdateTransactionCommand(
                    USER_ID,
                    new BigDecimal("200.00"),
                    LocalDateTime.now(),
                    TransactionType.INCOME,
                    "Updated",
                    null
            );

            when(transactionRepository.findByIdAndUserId(transaction.getId(), USER_ID))
                    .thenReturn(Optional.of(transaction));
            when(transactionRepository.save(transaction)).thenReturn(transaction);

            Transaction result = transactionService.update(transaction.getId(), command);

            assertThat(result.getAmount()).isEqualByComparingTo("200.00");
            assertThat(result.getDescription()).isEqualTo("Updated");
            verify(transactionRepository).save(transaction);
        }

        @Test
        @DisplayName("Should throw when transaction does not exist")
        void shouldThrowWhenTransactionDoesNotExist() {
            UUID transactionId = UUID.randomUUID();
            UpdateTransactionCommand command = new UpdateTransactionCommand(
                    USER_ID,
                    new BigDecimal("200.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Updated",
                    null
            );

            when(transactionRepository.findByIdAndUserId(transactionId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.update(transactionId, command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw when new category does not belong to user")
        void shouldThrowWhenNewCategoryDoesNotBelongToUser() {
            Transaction transaction = existingTransaction();
            UUID newCategoryId = UUID.randomUUID();
            UpdateTransactionCommand command = new UpdateTransactionCommand(
                    USER_ID,
                    new BigDecimal("200.00"),
                    LocalDateTime.now(),
                    TransactionType.EXPENSE,
                    "Updated",
                    newCategoryId
            );

            when(transactionRepository.findByIdAndUserId(transaction.getId(), USER_ID))
                    .thenReturn(Optional.of(transaction));
            when(categoryRepository.findByIdAndUserId(newCategoryId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.update(transaction.getId(), command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("deleteByIdAndUserId")
    class Delete {

        @Test
        @DisplayName("Should deactivate and save transaction")
        void shouldDeactivateAndSaveTransaction() {
            Transaction transaction = existingTransaction();

            when(transactionRepository.findByIdAndUserId(transaction.getId(), USER_ID))
                    .thenReturn(Optional.of(transaction));

            transactionService.deleteByIdAndUserId(transaction.getId(), USER_ID);

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());

            assertThat(captor.getValue().isActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw when transaction does not exist")
        void shouldThrowWhenTransactionDoesNotExist() {
            UUID transactionId = UUID.randomUUID();

            when(transactionRepository.findByIdAndUserId(transactionId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    transactionService.deleteByIdAndUserId(transactionId, USER_ID)
            ).isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("unassignCategory")
    class UnassignCategory {

        @Test
        @DisplayName("Should delegate to repository")
        void shouldDelegateToRepository() {
            transactionService.unassignCategory(CATEGORY_ID, USER_ID);

            ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(transactionRepository).clearCategoryFromActiveTransactions(
                    eq(CATEGORY_ID),
                    eq(USER_ID),
                    captor.capture()
            );

            assertThat(captor.getValue()).isNotNull();
        }
    }

    @Nested
    @DisplayName("queries")
    class Queries {

        @Test
        @DisplayName("Should return transaction when getByIdAndUserId finds it")
        void shouldReturnTransactionWhenGetByIdAndUserIdFindsIt() {
            Transaction transaction = existingTransaction();

            when(transactionRepository.findByIdAndUserId(transaction.getId(), USER_ID))
                    .thenReturn(Optional.of(transaction));

            Transaction result = transactionService.getByIdAndUserId(transaction.getId(), USER_ID);

            assertThat(result).isSameAs(transaction);
        }

        @Test
        @DisplayName("Should throw when getByIdAndUserId does not find transaction")
        void shouldThrowWhenGetByIdAndUserIdDoesNotFindTransaction() {
            UUID transactionId = UUID.randomUUID();

            when(transactionRepository.findByIdAndUserId(transactionId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    transactionService.getByIdAndUserId(transactionId, USER_ID)
            ).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should delegate findAllByUserId to repository")
        void shouldDelegateFindAllByUserIdToRepository() {
            Transaction transaction = existingTransaction();
            List<Transaction> transactions = List.of(transaction);

            when(transactionRepository.findAllByUserId(USER_ID)).thenReturn(transactions);

            List<Transaction> result = transactionService.findAllByUserId(USER_ID);

            assertThat(result).containsExactly(transaction);
        }

        @Test
        @DisplayName("Should delegate countByUserIdAndFilters to repository")
        void shouldDelegateCountByUserIdAndFiltersToRepository() {
            when(transactionRepository.countByUserIdAndFilters(
                    USER_ID, "search", TransactionType.EXPENSE, null, null
            )).thenReturn(5L);

            long result = transactionService.countByUserIdAndFilters(
                    USER_ID, "search", TransactionType.EXPENSE, null, null
            );

            assertThat(result).isEqualTo(5L);
        }
    }
}