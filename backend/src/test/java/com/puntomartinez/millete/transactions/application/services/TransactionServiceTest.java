package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.UpdateTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UUID userId;
    private UUID transactionId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }

    private Transaction createTransaction() {
        Transaction tx = new Transaction();
        tx.setId(transactionId);
        tx.setUserId(userId);
        tx.setCategoryId(categoryId);
        tx.setAmount(new BigDecimal("50.00"));
        tx.setDate(LocalDateTime.now());
        tx.setType(Transaction.TransactionType.EXPENSE);
        tx.setDescription("Almuerzo");
        tx.setActive(true);
        return tx;
    }

    @Test
    void register_shouldSaveTransaction() {
        RegisterTransactionUseCase.RegisterTransactionCommand command =
                new RegisterTransactionUseCase.RegisterTransactionCommand(
                        userId, null, new BigDecimal("50.00"), LocalDateTime.now(),
                        Transaction.TransactionType.EXPENSE, "Almuerzo"
                );

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());

        RegisterTransactionUseCase.RegisterTransactionResult result = transactionService.register(command);

        assertNotNull(result.transaction());
        assertEquals(userId, result.transaction().getUserId());
        assertEquals(new BigDecimal("50.00"), result.transaction().getAmount());
        assertFalse(result.limitExceeded());
    }

    @Test
    void register_shouldCheckCategory_whenProvided() {
        RegisterTransactionUseCase.RegisterTransactionCommand command =
                new RegisterTransactionUseCase.RegisterTransactionCommand(
                        userId, categoryId, new BigDecimal("50.00"), LocalDateTime.now(),
                        Transaction.TransactionType.EXPENSE, "Almuerzo"
                );

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.register(command));
    }

    @Test
    void register_shouldDetectLimitExceeded() {
        Transaction existingIncome = createTransaction();
        existingIncome.setType(Transaction.TransactionType.INCOME);
        existingIncome.setAmount(new BigDecimal("1000.00"));
        existingIncome.setDate(LocalDateTime.now());

        RegisterTransactionUseCase.RegisterTransactionCommand command =
                new RegisterTransactionUseCase.RegisterTransactionCommand(
                        userId, null, new BigDecimal("800.00"), LocalDateTime.now(),
                        Transaction.TransactionType.EXPENSE, "Gasto grande"
                );

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(existingIncome));

        RegisterTransactionUseCase.RegisterTransactionResult result = transactionService.register(command);

        assertTrue(result.limitExceeded());
    }

    @Test
    void findAllByUserId_shouldReturnTransactions() {
        Transaction tx = createTransaction();
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(tx));

        List<Transaction> result = transactionService.findAllByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(transactionId, result.get(0).getId());
    }

    @Test
    void getByIdAndUserId_shouldReturnTransaction() {
        Transaction tx = createTransaction();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));

        Transaction result = transactionService.getByIdAndUserId(transactionId, userId);

        assertEquals(transactionId, result.getId());
    }

    @Test
    void getByIdAndUserId_shouldThrow_whenNotFound() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getByIdAndUserId(transactionId, userId));
    }

    @Test
    void getByIdAndUserId_shouldThrow_whenForbidden() {
        Transaction tx = createTransaction();
        tx.setUserId(UUID.randomUUID()); // Different user
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));

        assertThrows(ForbiddenOperationException.class, () -> transactionService.getByIdAndUserId(transactionId, userId));
    }

    @Test
    void update_shouldUpdateTransaction() {
        Transaction tx = createTransaction();
        UpdateTransactionUseCase.UpdateTransactionCommand command =
                new UpdateTransactionUseCase.UpdateTransactionCommand(
                        userId, new BigDecimal("100.00"), LocalDateTime.now(),
                        Transaction.TransactionType.INCOME, "Nueva descripción", null
                );

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.update(transactionId, command);

        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(Transaction.TransactionType.INCOME, result.getType());
        assertEquals("Nueva descripción", result.getDescription());
    }

    @Test
    void deleteByIdAndUserId_shouldDeactivateTransaction() {
        Transaction tx = createTransaction();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.deleteByIdAndUserId(transactionId, userId);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }
}
