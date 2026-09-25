package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.transactions.domain.ports.in.GetTransactionMetricsUseCase.MetricsCommand;
import com.puntomartinez.millete.transactions.domain.ports.in.GetTransactionMetricsUseCase.MetricsResult;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository.TransactionAggregates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionMetricsService")
class TransactionMetricsServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionPeriodService transactionPeriodService;

    private TransactionMetricsService service;

    @BeforeEach
    void setUp() {
        service = new TransactionMetricsService(transactionRepository, transactionPeriodService);

        LocalDateTime[] currentRange = {
                Instant.now().minusDays(30),
                Instant.now()
        };
        LocalDateTime[] previousRange = {
                Instant.now().minusDays(60),
                Instant.now().minusDays(30)
        };

        lenient().when(transactionPeriodService.getDateRange(any())).thenReturn(currentRange);
        lenient().when(transactionPeriodService.getPreviousPeriod(any())).thenReturn(previousRange);
    }

    @Nested
    @DisplayName("getMetrics")
    class GetMetrics {

        @Test
        @DisplayName("Should calculate income, expenses, balance and count")
        void shouldCalculateMetrics() {
            TransactionAggregates current = new TransactionAggregates(
                    new BigDecimal("2000.00"),
                    new BigDecimal("500.00"),
                    15L
            );
            TransactionAggregates previous = new TransactionAggregates(
                    new BigDecimal("1500.00"),
                    new BigDecimal("400.00"),
                    10L
            );

            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(current, previous);

            MetricsResult result = service.getMetrics(new MetricsCommand(USER_ID, "month"));

            assertThat(result.income()).isEqualByComparingTo("2000.00");
            assertThat(result.expenses()).isEqualByComparingTo("500.00");
            assertThat(result.balance()).isEqualByComparingTo("1500.00");
            assertThat(result.count()).isEqualTo(15L);
        }

        @Test
        @DisplayName("Should calculate trend when previous is positive")
        void shouldCalculateTrendWhenPreviousIsPositive() {
            TransactionAggregates current = new TransactionAggregates(
                    new BigDecimal("2000.00"),
                    new BigDecimal("500.00"),
                    15L
            );
            TransactionAggregates previous = new TransactionAggregates(
                    new BigDecimal("1000.00"),
                    new BigDecimal("500.00"),
                    10L
            );

            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(current, previous);

            MetricsResult result = service.getMetrics(new MetricsCommand(USER_ID, "month"));

            // income trend: (2000 - 1000) * 100 / 1000 = 100.0
            assertThat(result.incomeTrend()).isEqualTo(100.0);
            // expenses trend: (500 - 500) * 100 / 500 = 0.0
            assertThat(result.expensesTrend()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return 100 trend when previous is zero and current is positive")
        void shouldReturn100TrendWhenPreviousIsZeroAndCurrentIsPositive() {
            TransactionAggregates current = new TransactionAggregates(
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    5L
            );
            TransactionAggregates previous = new TransactionAggregates(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0L
            );

            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(current, previous);

            MetricsResult result = service.getMetrics(new MetricsCommand(USER_ID, "month"));

            assertThat(result.incomeTrend()).isEqualTo(100.0);
            assertThat(result.expensesTrend()).isEqualTo(0.0);
            assertThat(result.countTrend()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should return 0 trend when both current and previous are zero")
        void shouldReturn0TrendWhenBothAreZero() {
            TransactionAggregates current = new TransactionAggregates(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0L
            );
            TransactionAggregates previous = new TransactionAggregates(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0L
            );

            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(current, previous);

            MetricsResult result = service.getMetrics(new MetricsCommand(USER_ID, "month"));

            assertThat(result.incomeTrend()).isEqualTo(0.0);
            assertThat(result.expensesTrend()).isEqualTo(0.0);
            assertThat(result.balanceTrend()).isEqualTo(0.0);
            assertThat(result.countTrend()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should calculate count trend correctly")
        void shouldCalculateCountTrendCorrectly() {
            TransactionAggregates current = new TransactionAggregates(
                    BigDecimal.TEN,
                    BigDecimal.TEN,
                    20L
            );
            TransactionAggregates previous = new TransactionAggregates(
                    BigDecimal.TEN,
                    BigDecimal.TEN,
                    10L
            );

            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(current, previous);

            MetricsResult result = service.getMetrics(new MetricsCommand(USER_ID, "month"));

            // count trend: ((20 - 10) / 10) * 100 = 100.0
            assertThat(result.countTrend()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should handle negative balance trend")
        void shouldHandleNegativeBalanceTrend() {
            TransactionAggregates current = new TransactionAggregates(
                    new BigDecimal("500.00"),
                    new BigDecimal("1000.00"),
                    10L
            );
            TransactionAggregates previous = new TransactionAggregates(
                    new BigDecimal("1000.00"),
                    new BigDecimal("500.00"),
                    10L
            );

            when(transactionRepository.getAggregatesByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(current, previous);

            MetricsResult result = service.getMetrics(new MetricsCommand(USER_ID, "month"));

            // current balance = -500, previous balance = 500
            // trend = (-500 - 500) * 100 / 500 = -200.0
            assertThat(result.balanceTrend()).isEqualTo(-200.0);
        }
    }
}