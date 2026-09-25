package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardBudgetsResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardBudgetService")
class DashboardBudgetServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock
    private TransactionQueryPort transactionQueryPort;

    @Mock
    private CategoryQueryPort categoryQueryPort;

    @Mock
    private DashboardPeriodService dashboardPeriodService;

    private DashboardBudgetService service;

    @BeforeEach
    void setUp() {
        service = new DashboardBudgetService(
                transactionQueryPort,
                categoryQueryPort,
                dashboardPeriodService
        );
        LocalDate[] range = {
                LocalDate.now().minusDays(30),
                LocalDate.now()
        };
        org.mockito.Mockito.lenient()
                .when(dashboardPeriodService.getDateRange(any()))
                .thenReturn(range);
    }

    @Nested
    @DisplayName("getBudgets")
    class GetBudgets {

        @Test
        @DisplayName("Should calculate spent and percentage for budgeted categories")
        void shouldCalculateSpentAndPercentage() {
            TransactionQueryPort.TransactionData tx =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Groceries", CATEGORY_ID,
                            new BigDecimal("75.00"),
                            Instant.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(tx));

            when(categoryQueryPort.findCategoriesWithBudgetByUserId(USER_ID))
                    .thenReturn(List.of(
                            new CategoryQueryPort.CategoryData(
                                    CATEGORY_ID, "Food", "#FF0000",
                                    new BigDecimal("100.00")
                            )
                    ));

            DashboardBudgetsResponseDTO result =
                    service.getBudgets(USER_ID, "month");

            assertThat(result.period()).isEqualTo("month");
            assertThat(result.budgets()).hasSize(1);
            assertThat(result.budgets().get(0).spent())
                    .isEqualByComparingTo("75.00");
            assertThat(result.budgets().get(0).percentage())
                    .isEqualTo(75.0);
        }

        @Test
        @DisplayName("Should order over-budget items first")
        void shouldOrderOverBudgetFirst() {
            UUID catId1 = UUID.randomUUID();
            UUID catId2 = UUID.randomUUID();

            TransactionQueryPort.TransactionData tx1 =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Over", catId1,
                            new BigDecimal("150.00"),
                            Instant.now(), "EXPENSE"
                    );
            TransactionQueryPort.TransactionData tx2 =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Under", catId2,
                            new BigDecimal("50.00"),
                            Instant.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(tx1, tx2));

            when(categoryQueryPort.findCategoriesWithBudgetByUserId(USER_ID))
                    .thenReturn(List.of(
                            new CategoryQueryPort.CategoryData(
                                    catId1, "Over", "#FF0000",
                                    new BigDecimal("100.00")
                            ),
                            new CategoryQueryPort.CategoryData(
                                    catId2, "Under", "#00FF00",
                                    new BigDecimal("100.00")
                            )
                    ));

            DashboardBudgetsResponseDTO result =
                    service.getBudgets(USER_ID, "month");

            assertThat(result.budgets()).hasSize(2);
            assertThat(result.budgets().get(0).percentage())
                    .isGreaterThanOrEqualTo(100.0);
        }

        @Test
        @DisplayName("Should limit results to 5 items")
        void shouldLimitToFiveItems() {
            List<CategoryQueryPort.CategoryData> manyCategories =
                    java.util.stream.IntStream.range(0, 10)
                            .mapToObj(i -> new CategoryQueryPort.CategoryData(
                                    UUID.randomUUID(),
                                    "Cat" + i,
                                    "#FF0000",
                                    new BigDecimal("100.00")
                            ))
                            .toList();

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            when(categoryQueryPort.findCategoriesWithBudgetByUserId(USER_ID))
                    .thenReturn(manyCategories);

            DashboardBudgetsResponseDTO result =
                    service.getBudgets(USER_ID, "month");

            assertThat(result.budgets()).hasSizeLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Should return empty when no budgeted categories")
        void shouldReturnEmptyWhenNoBudgetedCategories() {
            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            when(categoryQueryPort.findCategoriesWithBudgetByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            DashboardBudgetsResponseDTO result =
                    service.getBudgets(USER_ID, "month");

            assertThat(result.budgets()).isEmpty();
        }

        @Test
        @DisplayName("Should return zero percentage when budget limit is zero")
        void shouldReturnZeroPercentageWhenBudgetIsZero() {
            TransactionQueryPort.TransactionData tx =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Expense", CATEGORY_ID,
                            new BigDecimal("50.00"),
                            Instant.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(tx));

            when(categoryQueryPort.findCategoriesWithBudgetByUserId(USER_ID))
                    .thenReturn(List.of(
                            new CategoryQueryPort.CategoryData(
                                    CATEGORY_ID, "Food", "#FF0000",
                                    BigDecimal.ZERO
                            )
                    ));

            DashboardBudgetsResponseDTO result =
                    service.getBudgets(USER_ID, "month");

            assertThat(result.budgets().get(0).percentage()).isEqualTo(0.0);
        }
    }
}