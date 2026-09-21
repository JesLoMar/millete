package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.SavingsGoalQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService")
class DashboardServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private TransactionQueryPort transactionQueryPort;

    @Mock
    private CategoryQueryPort categoryQueryPort;

    @Mock
    private SavingsGoalQueryPort savingsGoalQueryPort;

    @Mock
    private DashboardPeriodService dashboardPeriodService;

    @Mock
    private DashboardHistoryService dashboardHistoryService;

    @Mock
    private DashboardCategoryService dashboardCategoryService;

    @Mock
    private DashboardBudgetService dashboardBudgetService;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(
                transactionQueryPort,
                categoryQueryPort,
                savingsGoalQueryPort,
                dashboardPeriodService,
                dashboardHistoryService,
                dashboardCategoryService,
                dashboardBudgetService
        );
    }

    @Nested
    @DisplayName("getMetrics")
    class GetMetrics {

        @Test
        @DisplayName("Should calculate income, expenses and balance")
        void shouldCalculateMetrics() {
            LocalDateTime[] currentRange = {
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now()
            };
            LocalDateTime[] previousRange = {
                    LocalDateTime.now().minusDays(60),
                    LocalDateTime.now().minusDays(30)
            };

            when(dashboardPeriodService.getDateRange("month"))
                    .thenReturn(currentRange);
            when(dashboardPeriodService.getPreviousPeriod("month"))
                    .thenReturn(previousRange);

            TransactionQueryPort.TransactionData income =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Salary", null,
                            new BigDecimal("2000.00"),
                            LocalDateTime.now(), "INCOME"
                    );
            TransactionQueryPort.TransactionData expense =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Food", null,
                            new BigDecimal("500.00"),
                            LocalDateTime.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), eq(currentRange[0]), eq(currentRange[1])
            )).thenReturn(List.of(income, expense));

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), eq(previousRange[0]), eq(previousRange[1])
            )).thenReturn(Collections.emptyList());

            DashboardMetricsResponseDTO result =
                    service.getMetrics(USER_ID, "month");

            assertThat(result.income()).isEqualByComparingTo("2000.00");
            assertThat(result.expenses()).isEqualByComparingTo("500.00");
            assertThat(result.balance()).isEqualByComparingTo("1500.00");
            assertThat(result.savings()).isEqualByComparingTo("1500.00");
        }

        @Test
        @DisplayName("Should return 100 trend when previous is zero and current is positive")
        void shouldReturn100TrendWhenPreviousIsZero() {
            LocalDateTime[] currentRange = {
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now()
            };
            LocalDateTime[] previousRange = {
                    LocalDateTime.now().minusDays(60),
                    LocalDateTime.now().minusDays(30)
            };

            when(dashboardPeriodService.getDateRange("month"))
                    .thenReturn(currentRange);
            when(dashboardPeriodService.getPreviousPeriod("month"))
                    .thenReturn(previousRange);

            TransactionQueryPort.TransactionData income =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Salary", null,
                            new BigDecimal("1000.00"),
                            LocalDateTime.now(), "INCOME"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), eq(currentRange[0]), eq(currentRange[1])
            )).thenReturn(List.of(income));

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), eq(previousRange[0]), eq(previousRange[1])
            )).thenReturn(Collections.emptyList());

            DashboardMetricsResponseDTO result =
                    service.getMetrics(USER_ID, "month");

            assertThat(result.incomeTrend()).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("getHistory")
    class GetHistory {

        @Test
        @DisplayName("Should delegate to DashboardHistoryService")
        void shouldDelegateToHistoryService() {
            DashboardHistoryResponseDTO expected =
                    new DashboardHistoryResponseDTO(
                            "month", List.of("Sem 1"), List.of(BigDecimal.TEN)
                    );
            when(dashboardHistoryService.getHistory(USER_ID, "month"))
                    .thenReturn(expected);

            DashboardHistoryResponseDTO result =
                    service.getHistory(USER_ID, "month");

            assertThat(result).isSameAs(expected);
            verify(dashboardHistoryService).getHistory(USER_ID, "month");
        }
    }

    @Nested
    @DisplayName("getCategories")
    class GetCategories {

        @Test
        @DisplayName("Should delegate to DashboardCategoryService")
        void shouldDelegateToCategoryService() {
            DashboardCategoriesResponseDTO expected =
                    new DashboardCategoriesResponseDTO(
                            BigDecimal.TEN, Collections.emptyList()
                    );
            when(dashboardCategoryService.getCategories(USER_ID, "month"))
                    .thenReturn(expected);

            DashboardCategoriesResponseDTO result =
                    service.getCategories(USER_ID, "month");

            assertThat(result).isSameAs(expected);
            verify(dashboardCategoryService).getCategories(USER_ID, "month");
        }
    }

    @Nested
    @DisplayName("getBudgets")
    class GetBudgets {

        @Test
        @DisplayName("Should delegate to DashboardBudgetService")
        void shouldDelegateToBudgetService() {
            DashboardBudgetsResponseDTO expected =
                    new DashboardBudgetsResponseDTO("month", Collections.emptyList());
            when(dashboardBudgetService.getBudgets(USER_ID, "month"))
                    .thenReturn(expected);

            DashboardBudgetsResponseDTO result =
                    service.getBudgets(USER_ID, "month");

            assertThat(result).isSameAs(expected);
            verify(dashboardBudgetService).getBudgets(USER_ID, "month");
        }
    }

    @Nested
    @DisplayName("getRecentTransactions")
    class GetRecentTransactions {

        @Test
        @DisplayName("Should map transactions with category info")
        void shouldMapTransactionsWithCategoryInfo() {
            UUID categoryId = UUID.randomUUID();
            TransactionQueryPort.TransactionData tx =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Groceries", categoryId,
                            new BigDecimal("50.00"),
                            LocalDateTime.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findRecentByUserId(USER_ID, 5))
                    .thenReturn(List.of(tx));

            when(categoryQueryPort.findByIdsAndUserId(USER_ID, List.of(categoryId)))
                    .thenReturn(List.of(
                            new CategoryQueryPort.CategoryData(
                                    categoryId, "Food", "#FF0000", null
                            )
                    ));

            DashboardTransactionsResponseDTO result =
                    service.getRecentTransactions(USER_ID, 5);

            assertThat(result.transactions()).hasSize(1);
            assertThat(result.transactions().get(0).category()).isEqualTo("Food");
            assertThat(result.transactions().get(0).categoryColor()).isEqualTo("#FF0000");
        }

        @Test
        @DisplayName("Should use Sin categoria when category is null")
        void shouldUseSinCategoriaWhenCategoryIsNull() {
            TransactionQueryPort.TransactionData tx =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Unknown", null,
                            new BigDecimal("50.00"),
                            LocalDateTime.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findRecentByUserId(USER_ID, 5))
                    .thenReturn(List.of(tx));

            DashboardTransactionsResponseDTO result =
                    service.getRecentTransactions(USER_ID, 5);

            assertThat(result.transactions().get(0).category())
                    .isEqualTo("Sin categoría");
        }

        @Test
        @DisplayName("Should not call findByIdsAndUserId when no categoryIds")
        void shouldNotCallFindByIdsWhenNoCategories() {
            TransactionQueryPort.TransactionData tx =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Unknown", null,
                            new BigDecimal("50.00"),
                            LocalDateTime.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findRecentByUserId(USER_ID, 5))
                    .thenReturn(List.of(tx));

            service.getRecentTransactions(USER_ID, 5);

            verify(categoryQueryPort, never()).findByIdsAndUserId(any(), any());
        }
    }

    @Nested
    @DisplayName("getSavingsGoals")
    class GetSavingsGoals {

        @Test
        @DisplayName("Should return goals sorted by priority and createdAt")
        void shouldSortByPriorityAndCreatedAt() {
            SavingsGoalQueryPort.SavingsGoalData highGoal =
                    new SavingsGoalQueryPort.SavingsGoalData(
                            UUID.randomUUID(), "High Goal",
                            new BigDecimal("1000"), new BigDecimal("500"),
                            LocalDate.now().plusDays(30), "HIGH",
                            LocalDateTime.now().minusDays(1)
                    );
            SavingsGoalQueryPort.SavingsGoalData lowGoal =
                    new SavingsGoalQueryPort.SavingsGoalData(
                            UUID.randomUUID(), "Low Goal",
                            new BigDecimal("500"), new BigDecimal("100"),
                            LocalDate.now().plusDays(60), "LOW",
                            LocalDateTime.now().minusDays(2)
                    );

            when(savingsGoalQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(lowGoal, highGoal));

            DashboardGoalsResponseDTO result =
                    service.getSavingsGoals(USER_ID);

            assertThat(result.goals()).hasSize(2);
            assertThat(result.goals().get(0).name()).isEqualTo("High Goal");
        }

        @Test
        @DisplayName("Should limit to 20 goals")
        void shouldLimitTo20Goals() {
            List<SavingsGoalQueryPort.SavingsGoalData> manyGoals =
                    java.util.stream.IntStream.range(0, 25)
                            .mapToObj(i -> new SavingsGoalQueryPort.SavingsGoalData(
                                    UUID.randomUUID(), "Goal " + i,
                                    new BigDecimal("1000"), new BigDecimal("500"),
                                    LocalDate.now().plusDays(30), "MEDIUM",
                                    LocalDateTime.now().minusDays(i)
                            ))
                            .toList();

            when(savingsGoalQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(manyGoals);

            DashboardGoalsResponseDTO result =
                    service.getSavingsGoals(USER_ID);

            assertThat(result.goals()).hasSize(20);
        }

        @Test
        @DisplayName("Should return empty when no goals")
        void shouldReturnEmptyWhenNoGoals() {
            when(savingsGoalQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            DashboardGoalsResponseDTO result =
                    service.getSavingsGoals(USER_ID);

            assertThat(result.goals()).isEmpty();
        }

        @Test
        @DisplayName("Should use default icon when priority is unrecognized")
        void shouldUseDefaultIconWhenPriorityIsUnrecognized() {
            SavingsGoalQueryPort.SavingsGoalData goal =
                    new SavingsGoalQueryPort.SavingsGoalData(
                            UUID.randomUUID(), "Unknown Priority",
                            new BigDecimal("1000"), new BigDecimal("500"),
                            LocalDate.now().plusDays(30), "INVALID",  // ← valor no reconocido
                            LocalDateTime.now()
                    );

            when(savingsGoalQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(goal));

            DashboardGoalsResponseDTO result =
                    service.getSavingsGoals(USER_ID);

            assertThat(result.goals()).hasSize(1);
            assertThat(result.goals().get(0).icon()).isEqualTo("default");
        }
    }
}