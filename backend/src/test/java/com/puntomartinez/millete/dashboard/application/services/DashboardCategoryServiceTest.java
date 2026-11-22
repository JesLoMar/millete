package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.CategoryExpenseItemResponseDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardCategoryServiceTest {
    @Test
    void groupsIncomeAndExpensesIndependentlyWithoutChangingTheExpenseBreakdown() {
        UUID user = UUID.randomUUID();
        UUID category = UUID.randomUUID();
        LocalDate day = LocalDate.of(2026, 11, 22);
        LocalDate start = day.withDayOfMonth(1);
        var transactions = mock(TransactionQueryPort.class);
        var categories = mock(CategoryQueryPort.class);
        var periods = mock(DashboardPeriodService.class);
        when(periods.getDateRange("month", user)).thenReturn(new LocalDate[]{start, day});
        when(categories.findByUserId(user)).thenReturn(List.of(
                new CategoryQueryPort.CategoryData(category, "Main", "#000000", null)
        ));
        when(transactions.findByUserIdAndDateBetween(user, start, day)).thenReturn(List.of(
                transaction(day, category, "-98", "EXPENSE"),
                transaction(day, null, "-2", "EXPENSE"),
                transaction(day, category, "150", "INCOME"),
                transaction(day, null, "50", "INCOME")
        ));

        var response = new DashboardCategoryService(transactions, categories, periods).getCategories(user, "month");

        assertThat(response.totalExpenses()).isEqualByComparingTo("100");
        assertThat(response.categories()).extracting(CategoryExpenseItemResponseDTO::percentage).containsExactly(98.0, 2.0);
        assertThat(response.categories()).extracting(CategoryExpenseItemResponseDTO::transactionCount).containsExactly(1, 1);
        assertThat(response.totalIncome()).isEqualByComparingTo("200");
        assertThat(response.incomeCategories()).extracting(CategoryExpenseItemResponseDTO::percentage)
                .containsExactly(75.0, 25.0);
        assertThat(response.incomeCategories()).extracting(CategoryExpenseItemResponseDTO::transactionCount)
                .containsExactly(1, 1);
        assertThat(response.incomeCategories()).extracting(CategoryExpenseItemResponseDTO::categoryId)
                .containsExactly(category, null);
        verify(transactions).findByUserIdAndDateBetween(user, start, day);
        verify(categories).findByUserId(user);
    }

    @Test
    void returnsEmptyListsForAWindowWithoutTransactions() {
        UUID user = UUID.randomUUID();
        LocalDate day = LocalDate.of(2026, 11, 22);
        var transactions = mock(TransactionQueryPort.class);
        var categories = mock(CategoryQueryPort.class);
        var periods = mock(DashboardPeriodService.class);
        when(periods.getDateRange("week", user)).thenReturn(new LocalDate[]{day.minusDays(6), day});
        when(transactions.findByUserIdAndDateBetween(user, day.minusDays(6), day)).thenReturn(List.of());
        when(categories.findByUserId(user)).thenReturn(List.of());

        var response = new DashboardCategoryService(transactions, categories, periods).getCategories(user, "week");
        assertThat(response.totalExpenses()).isZero();
        assertThat(response.totalIncome()).isZero();
        assertThat(response.categories()).isEmpty();
        assertThat(response.incomeCategories()).isEmpty();
    }

    private TransactionQueryPort.TransactionData transaction(LocalDate date, UUID category, String amount, String type) {
        return new TransactionQueryPort.TransactionData(UUID.randomUUID(), type, category, new BigDecimal(amount), date, type);
    }
}
