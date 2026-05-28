package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.in.GetTransactionMetricsUseCase;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionMetricsResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionMetricsService - Métricas de transacciones")
class TransactionMetricsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionMetricsService metricsService;

    private final UUID userId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("Debe calcular métricas con ingresos y gastos")
    void shouldCalculateMetrics() {
        Transaction income = mock(Transaction.class);
        when(income.getType()).thenReturn(Transaction.TransactionType.INCOME);
        when(income.getAmount()).thenReturn(new BigDecimal("1000.00"));
        when(income.isActive()).thenReturn(true);

        Transaction expense = mock(Transaction.class);
        when(expense.getType()).thenReturn(Transaction.TransactionType.EXPENSE);
        when(expense.getAmount()).thenReturn(new BigDecimal("-300.00"));
        when(expense.isActive()).thenReturn(true);

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(income, expense));

        GetTransactionMetricsUseCase.MetricsCommand command = new GetTransactionMetricsUseCase.MetricsCommand(userId, "month");

        TransactionMetricsResponseDTO result = metricsService.getMetrics(command);

        assertThat(result.income()).isEqualByComparingTo("1000.00");
        assertThat(result.expenses()).isEqualByComparingTo("300.00");
        assertThat(result.balance()).isEqualByComparingTo("700.00");
        assertThat(result.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe calcular métricas sin transacciones")
    void shouldCalculateMetricsWithNoTransactions() {
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of());

        GetTransactionMetricsUseCase.MetricsCommand command = new GetTransactionMetricsUseCase.MetricsCommand(userId, "month");

        TransactionMetricsResponseDTO result = metricsService.getMetrics(command);

        assertThat(result.income()).isEqualByComparingTo("0");
        assertThat(result.expenses()).isEqualByComparingTo("0");
        assertThat(result.balance()).isEqualByComparingTo("0");
        assertThat(result.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debe lanzar error con período inválido")
    void shouldThrowWithInvalidPeriod() {
        GetTransactionMetricsUseCase.MetricsCommand command = new GetTransactionMetricsUseCase.MetricsCommand(userId, "invalid");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> metricsService.getMetrics(command))
                .withMessage("Invalid period: invalid");
    }
}