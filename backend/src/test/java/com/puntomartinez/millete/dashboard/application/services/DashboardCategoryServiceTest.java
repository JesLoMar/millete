package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardCategoriesResponseDTO;
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
@DisplayName("DashboardCategoryService")
class DashboardCategoryServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID_1 = UUID.randomUUID();
    private static final UUID CATEGORY_ID_2 = UUID.randomUUID();

    @Mock
    private TransactionQueryPort transactionQueryPort;

    @Mock
    private CategoryQueryPort categoryQueryPort;

    @Mock
    private DashboardPeriodService dashboardPeriodService;

    private DashboardCategoryService service;

    @BeforeEach
    void setUp() {
        service = new DashboardCategoryService(
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
    @DisplayName("getCategories")
    class GetCategories {

        @Test
        @DisplayName("Should aggregate expenses by category")
        void shouldAggregateExpensesByCategory() {
            TransactionQueryPort.TransactionData tx1 =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Groceries", CATEGORY_ID_1,
                            new BigDecimal("100.00"),
                            Instant.now(), "EXPENSE"
                    );
            TransactionQueryPort.TransactionData tx2 =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Transport", CATEGORY_ID_2,
                            new BigDecimal("50.00"),
                            Instant.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(tx1, tx2));

            when(categoryQueryPort.findByUserId(USER_ID)).thenReturn(List.of(
                    new CategoryQueryPort.CategoryData(
                            CATEGORY_ID_1, "Food", "#FF0000", null
                    ),
                    new CategoryQueryPort.CategoryData(
                            CATEGORY_ID_2, "Transport", "#00FF00", null
                    )
            ));

            DashboardCategoriesResponseDTO result =
                    service.getCategories(USER_ID, "month");

            assertThat(result.totalExpenses())
                    .isEqualByComparingTo("150.00");
            assertThat(result.categories()).hasSize(2);
        }

        @Test
        @DisplayName("Should group orphan transactions under Sin categoria")
        void shouldGroupOrphanTransactions() {
            TransactionQueryPort.TransactionData orphanTx =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Orphan", null,
                            new BigDecimal("30.00"),
                            Instant.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(orphanTx));

            when(categoryQueryPort.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            DashboardCategoriesResponseDTO result =
                    service.getCategories(USER_ID, "month");

            assertThat(result.categories())
                    .anySatisfy(item ->
                            assertThat(item.name()).isEqualTo("Sin categoría")
                    );
        }

        @Test
        @DisplayName("Should sort categories by amount descending")
        void shouldSortByAmountDescending() {
            TransactionQueryPort.TransactionData tx1 =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Small", CATEGORY_ID_1,
                            new BigDecimal("10.00"),
                            Instant.now(), "EXPENSE"
                    );
            TransactionQueryPort.TransactionData tx2 =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Big", CATEGORY_ID_2,
                            new BigDecimal("90.00"),
                            Instant.now(), "EXPENSE"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(tx1, tx2));

            when(categoryQueryPort.findByUserId(USER_ID)).thenReturn(List.of(
                    new CategoryQueryPort.CategoryData(
                            CATEGORY_ID_1, "Small", "#FF0000", null
                    ),
                    new CategoryQueryPort.CategoryData(
                            CATEGORY_ID_2, "Big", "#00FF00", null
                    )
            ));

            DashboardCategoriesResponseDTO result =
                    service.getCategories(USER_ID, "month");

            assertThat(result.categories()).isNotEmpty();
            BigDecimal firstAmount = result.categories().get(0).amount();
            BigDecimal lastAmount =
                    result.categories().get(result.categories().size() - 1).amount();
            assertThat(firstAmount).isGreaterThanOrEqualTo(lastAmount);
        }

        @Test
        @DisplayName("Should return empty categories when no expenses")
        void shouldReturnEmptyWhenNoExpenses() {
            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(Collections.emptyList());

            when(categoryQueryPort.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            DashboardCategoriesResponseDTO result =
                    service.getCategories(USER_ID, "month");

            assertThat(result.totalExpenses())
                    .isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.categories()).isEmpty();
        }

        @Test
        @DisplayName("Should ignore INCOME transactions")
        void shouldIgnoreIncomeTransactions() {
            TransactionQueryPort.TransactionData incomeTx =
                    new TransactionQueryPort.TransactionData(
                            UUID.randomUUID(), "Salary", CATEGORY_ID_1,
                            new BigDecimal("2000.00"),
                            Instant.now(), "INCOME"
                    );

            when(transactionQueryPort.findByUserIdAndDateBetween(
                    eq(USER_ID), any(), any()
            )).thenReturn(List.of(incomeTx));

            when(categoryQueryPort.findByUserId(USER_ID)).thenReturn(List.of(
                    new CategoryQueryPort.CategoryData(
                            CATEGORY_ID_1, "Salary", "#FF0000", null
                    )
            ));

            DashboardCategoriesResponseDTO result =
                    service.getCategories(USER_ID, "month");

            assertThat(result.totalExpenses())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}