package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardHistoryResponseDTO;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardHistoryService")
class DashboardHistoryServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private TransactionQueryPort transactionQueryPort;

    private DashboardHistoryService service;

    @BeforeEach
    void setUp() {
        service = new DashboardHistoryService(transactionQueryPort);
    }

    @Nested
    @DisplayName("getHistory - week")
    class WeeklyHistory {

        @Test
        @DisplayName("Should return 7 labels for weekly history")
        void shouldReturnSevenLabels() {
            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            DashboardHistoryResponseDTO result =
                    service.getHistory(USER_ID, "week");

            assertThat(result.period()).isEqualTo("week");
            assertThat(result.labels()).hasSize(7);
            assertThat(result.data()).hasSize(7);
        }

        @Test
        @DisplayName("Should sum only EXPENSE transactions")
        void shouldSumOnlyExpenseTransactions() {
            LocalDate today = LocalDate.now();
            LocalDateTime todayStart = today.atStartOfDay();

            TransactionQueryPort.TransactionData expense =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Expense", null,
                            new BigDecimal("50.00"), todayStart, "EXPENSE"
                    );
            TransactionQueryPort.TransactionData income =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Income", null,
                            new BigDecimal("100.00"), todayStart, "INCOME"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(expense, income));

            DashboardHistoryResponseDTO result =
                    service.getHistory(USER_ID, "week");

            BigDecimal totalData = result.data().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(totalData).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("Should return zeros when no transactions exist")
        void shouldReturnZerosWhenNoTransactions() {
            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            DashboardHistoryResponseDTO result =
                    service.getHistory(USER_ID, "week");

            assertThat(result.data())
                    .allSatisfy(amount ->
                            assertThat(amount).isEqualByComparingTo(BigDecimal.ZERO)
                    );
        }
    }

    @Nested
    @DisplayName("getHistory - month")
    class MonthlyHistory {

        @Test
        @DisplayName("Should return weekly blocks for monthly history")
        void shouldReturnWeeklyBlocks() {
            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            DashboardHistoryResponseDTO result =
                    service.getHistory(USER_ID, "month");

            assertThat(result.period()).isEqualTo("month");
            assertThat(result.labels()).isNotEmpty();
            assertThat(result.labels().get(0)).isEqualTo("Sem 1");
            assertThat(result.data()).hasSameSizeAs(result.labels());
        }

        @Test
        @DisplayName("Should have at most 5 week blocks")
        void shouldHaveAtMostFiveWeekBlocks() {
            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            DashboardHistoryResponseDTO result =
                    service.getHistory(USER_ID, "month");

            assertThat(result.labels()).hasSizeLessThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("getHistory - year")
    class YearlyHistory {

        @Test
        @DisplayName("Should return monthly labels up to current month")
        void shouldReturnMonthlyLabelsUpToCurrentMonth() {
            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            DashboardHistoryResponseDTO result =
                    service.getHistory(USER_ID, "year");

            assertThat(result.period()).isEqualTo("year");
            int currentMonth = LocalDate.now().getMonthValue();
            assertThat(result.labels()).hasSize(currentMonth);
            assertThat(result.data()).hasSize(currentMonth);
        }

        @Test
        @DisplayName("Should not include future months")
        void shouldNotIncludeFutureMonths() {
            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            DashboardHistoryResponseDTO result =
                    service.getHistory(USER_ID, "year");

            int currentMonth = LocalDate.now().getMonthValue();
            assertThat(result.labels()).hasSizeLessThanOrEqualTo(currentMonth);
        }
    }

    @Nested
    @DisplayName("getHistory - invalid period")
    class InvalidPeriod {

        @Test
        @DisplayName("Should throw for invalid period")
        void shouldThrowForInvalidPeriod() {
            assertThatThrownBy(() ->
                    service.getHistory(USER_ID, "invalid")
            ).isInstanceOf(InvalidInputException.class);
        }
    }
}