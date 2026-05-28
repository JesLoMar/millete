package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.UpdateTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService - Servicio de transacciones")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private final UUID userId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @Test
    @DisplayName("Registrar gasto")
    void shouldRegisterExpense() {
        RegisterTransactionUseCase.RegisterTransactionCommand command = new RegisterTransactionUseCase.RegisterTransactionCommand(
                userId, categoryId, new BigDecimal("-50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Compra");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterTransactionUseCase.RegisterTransactionResult result = transactionService.register(command);

        assertThat(result.transaction().getDescription()).isEqualTo("Compra");
        assertThat(result.transaction().isActive()).isTrue();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Registrar con categoría inexistente lanza error")
    void shouldThrowWhenCategoryNotFound() {
        RegisterTransactionUseCase.RegisterTransactionCommand command = new RegisterTransactionUseCase.RegisterTransactionCommand(
                userId, categoryId, new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Compra");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatRuntimeException()
                .isThrownBy(() -> transactionService.register(command))
                .withMessage("Category does not exist.");
    }

    @Test
    @DisplayName("Listar transacciones por usuario")
    void shouldFindAllByUserId() {
        Transaction tx1 = mock(Transaction.class);
        Transaction tx2 = mock(Transaction.class);
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(tx1, tx2));

        List<Transaction> result = transactionService.findAllByUserId(userId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Obtener transacción por ID con usuario correcto")
    void shouldGetByIdAndUserId() {
        UUID id = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);
        when(tx.getUserId()).thenReturn(userId);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));

        Transaction result = transactionService.getByIdAndUserId(id, userId);

        assertThat(result).isEqualTo(tx);
    }

    @Test
    @DisplayName("Obtener transacción de otro usuario lanza error")
    void shouldThrowWhenGettingOtherUserTransaction() {
        UUID id = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);
        when(tx.getUserId()).thenReturn(UUID.randomUUID());
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));

        assertThatRuntimeException()
                .isThrownBy(() -> transactionService.getByIdAndUserId(id, userId))
                .withMessage("You do not have permission to view this transaction.");
    }

    @Test
    @DisplayName("Actualizar transacción")
    void shouldUpdateTransaction() {
        UUID id = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);
        when(tx.getUserId()).thenReturn(userId);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTransactionUseCase.UpdateTransactionCommand command = new UpdateTransactionUseCase.UpdateTransactionCommand(
                userId, new BigDecimal("100.00"), LocalDateTime.now(),
                Transaction.TransactionType.INCOME, "Venta", categoryId);

        Transaction result = transactionService.update(id, command);

        verify(tx).updateDetails(any(), any(), any(), any(), any());
        verify(transactionRepository).save(tx);
    }

    @Test
    @DisplayName("Eliminar transacción (soft delete)")
    void shouldDeleteTransaction() {
        UUID id = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);
        when(tx.getUserId()).thenReturn(userId);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));

        transactionService.deleteByIdAndUserId(id, userId);

        verify(tx).deactivate();
        verify(transactionRepository).save(tx);
    }

    @Test
    @DisplayName("Eliminar transacción de otro usuario lanza error")
    void shouldThrowWhenDeletingOtherUserTransaction() {
        UUID id = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);
        when(tx.getUserId()).thenReturn(UUID.randomUUID());
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));

        assertThatRuntimeException()
                .isThrownBy(() -> transactionService.deleteByIdAndUserId(id, userId))
                .withMessage("You do not have permission to view this transaction.");
    }
}