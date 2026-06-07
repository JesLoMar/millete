package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService - Servicio de dashboard")
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Obtener métricas del dashboard")
    void shouldGetMetrics() {
        Transaction income = createTransaction(Transaction.TransactionType.INCOME, "1000.00");
        Transaction expense = createTransaction(Transaction.TransactionType.EXPENSE, "-300.00");

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(income, expense));

        var result = dashboardService.getMetrics(userId, "month");

        assertThat(result.income()).isEqualByComparingTo("1000.00");
        assertThat(result.expenses()).isEqualByComparingTo("300.00");
        assertThat(result.balance()).isEqualByComparingTo("700.00");
        assertThat(result.savings()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("Obtener métricas sin transacciones")
    void shouldGetMetricsWithNoTransactions() {
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of());

        var result = dashboardService.getMetrics(userId, "month");

        assertThat(result.income()).isEqualByComparingTo("0");
        assertThat(result.expenses()).isEqualByComparingTo("0");
        assertThat(result.balance()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Obtener gastos por categoría")
    void shouldGetCategories() {
        UUID categoryId = UUID.randomUUID();
        Transaction expense = mock(Transaction.class, RETURNS_DEEP_STUBS);
        when(expense.getType()).thenReturn(Transaction.TransactionType.EXPENSE);
        when(expense.getAmount()).thenReturn(new BigDecimal("-300.00"));
        when(expense.getCategoryId()).thenReturn(categoryId);

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Transporte");

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(expense));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        var result = dashboardService.getCategories(userId, "month");

        assertThat(result.totalExpenses()).isEqualByComparingTo("300.00");
        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().get(0).name()).isEqualTo("Transporte");
    }

    @Test
    @DisplayName("Obtener presupuestos")
    void shouldGetBudgets() {
        UUID categoryId = UUID.randomUUID();
        Category category = mock(Category.class);
        when(category.getId()).thenReturn(categoryId);
        when(category.getName()).thenReturn("Transporte");
        when(category.getBudgetLimit()).thenReturn(new BigDecimal("500.00"));

        Transaction expense = mock(Transaction.class);
        when(expense.getCategoryId()).thenReturn(categoryId);
        when(expense.getType()).thenReturn(Transaction.TransactionType.EXPENSE);
        when(expense.getAmount()).thenReturn(new BigDecimal("-300.00"));

        when(categoryRepository.findCategoriesWithBudgetByUserId(userId)).thenReturn(List.of(category));
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(expense));

        var result = dashboardService.getBudgets(userId, "month");

        assertThat(result.budgets()).hasSize(1);
        assertThat(result.budgets().get(0).category()).isEqualTo("Transporte");
        assertThat(result.budgets().get(0).spent()).isEqualByComparingTo("300.00");
        assertThat(result.budgets().get(0).limit()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Obtener presupuestos sin gastos no muestra nada")
    void shouldNotShowBudgetsWithNoSpending() {
        UUID categoryId = UUID.randomUUID();
        Category category = mock(Category.class);
        when(category.getId()).thenReturn(categoryId);
        when(category.getName()).thenReturn("Transporte");
        when(category.getBudgetLimit()).thenReturn(new BigDecimal("500.00"));

        when(categoryRepository.findCategoriesWithBudgetByUserId(userId)).thenReturn(List.of(category));
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of());

        var result = dashboardService.getBudgets(userId, "month");

        assertThat(result.budgets()).isEmpty();
    }

    @Test
    @DisplayName("Obtener transacciones recientes")
    void shouldGetRecentTransactions() {
        Transaction tx = createTransaction(Transaction.TransactionType.EXPENSE, "-50.00");
        // Estos stubs adicionales SÍ se usan en el método getRecentTransactions
        when(tx.getDescription()).thenReturn("Compra");
        when(tx.getCategoryId()).thenReturn(UUID.randomUUID());

        when(transactionRepository.findRecentByUserId(userId, 5)).thenReturn(List.of(tx));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(mock(Category.class)));

        var result = dashboardService.getRecentTransactions(userId, 5);

        assertThat(result.transactions()).hasSize(1);
    }

    @Test
    @DisplayName("Obtener métricas de inversiones")
    void shouldGetInvestmentMetrics() {
        Investment inv = mock(Investment.class);
        when(inv.isActive()).thenReturn(true);
        when(inv.getCurrentValue()).thenReturn(new BigDecimal("1500.00"));
        when(inv.getProfitOrLoss()).thenReturn(new BigDecimal("500.00"));
        when(inv.getInvestedCapital()).thenReturn(new BigDecimal("1000.00"));

        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of(inv));

        var result = dashboardService.getInvestmentMetrics(userId, "month");

        assertThat(result.portfolioValue()).isEqualByComparingTo("1500.00");
        assertThat(result.monthlyReturn()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Obtener distribución de inversiones")
    void shouldGetInvestmentDistribution() {
        Investment inv = mock(Investment.class);
        when(inv.isActive()).thenReturn(true);
        when(inv.getCurrentValue()).thenReturn(new BigDecimal("1000.00"));
        when(inv.getType()).thenReturn(Investment.InvestmentType.STOCK);

        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of(inv));

        var result = dashboardService.getInvestmentDistribution(userId, "month");

        assertThat(result.totalValue()).isEqualByComparingTo("1000.00");
        assertThat(result.distribution()).hasSize(1);
    }

    @Test
    @DisplayName("Período inválido lanza error en métricas")
    void shouldThrowWithInvalidPeriod() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> dashboardService.getMetrics(userId, "invalid"))
                .withMessage("Invalid period: invalid");
    }

    private Transaction createTransaction(Transaction.TransactionType type, String amount) {
        Transaction tx = mock(Transaction.class);
        lenient().when(tx.getType()).thenReturn(type);
        lenient().when(tx.getAmount()).thenReturn(new BigDecimal(amount));
        return tx;
    }

    @Test
    @DisplayName("Obtener historial semanal")
    void shouldGetWeeklyHistory() {
        Transaction expense = createTransaction(Transaction.TransactionType.EXPENSE, "-100.00");
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(expense));

        var result = dashboardService.getHistory(userId, "week");

        assertThat(result.period()).isEqualTo("week");
        assertThat(result.labels()).isNotEmpty();
        assertThat(result.data()).isNotEmpty();
    }

    @Test
    @DisplayName("Obtener historial mensual")
    void shouldGetMonthlyHistory() {
        Transaction expense = createTransaction(Transaction.TransactionType.EXPENSE, "-200.00");
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(expense));

        var result = dashboardService.getHistory(userId, "month");

        assertThat(result.period()).isEqualTo("month");
        assertThat(result.labels()).isNotEmpty();
    }

    @Test
    @DisplayName("Obtener historial anual")
    void shouldGetYearlyHistory() {
        Transaction expense = createTransaction(Transaction.TransactionType.EXPENSE, "-500.00");
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(expense));

        var result = dashboardService.getHistory(userId, "year");

        assertThat(result.period()).isEqualTo("year");
        assertThat(result.labels()).isNotEmpty();
    }

    @Test
    @DisplayName("Obtener historial con período inválido lanza error")
    void shouldThrowWithInvalidHistoryPeriod() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> dashboardService.getHistory(userId, "invalid"))
                .withMessage("Invalid period: invalid");
    }

    @Test
    @DisplayName("Obtener metas de ahorro")
    void shouldGetSavingsGoals() {
        Transaction income = createTransaction(Transaction.TransactionType.INCOME, "12000.00");
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(income));

        var result = dashboardService.getSavingsGoals(userId);

        assertThat(result.goals()).hasSize(2);
        assertThat(result.goals().get(0).name()).isEqualTo("Fondo de Emergencia");
        assertThat(result.goals().get(1).name()).isEqualTo("Vacaciones");
    }

    @Test
    @DisplayName("Obtener evolución semanal de inversiones")
    void shouldGetWeeklyInvestmentEvolution() {
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());

        var result = dashboardService.getInvestmentEvolution(userId, "week");

        assertThat(result.period()).isEqualTo("week");
        assertThat(result.labels()).isNotEmpty();
    }

    @Test
    @DisplayName("Obtener evolución mensual de inversiones")
    void shouldGetMonthlyInvestmentEvolution() {
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());

        var result = dashboardService.getInvestmentEvolution(userId, "month");

        assertThat(result.period()).isEqualTo("month");
    }

    @Test
    @DisplayName("Obtener evolución anual de inversiones")
    void shouldGetYearlyInvestmentEvolution() {
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());

        var result = dashboardService.getInvestmentEvolution(userId, "year");

        assertThat(result.period()).isEqualTo("year");
    }

    @Test
    @DisplayName("Evolución con período inválido lanza error")
    void shouldThrowWithInvalidEvolutionPeriod() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> dashboardService.getInvestmentEvolution(userId, "invalid"))
                .withMessage("Invalid period: invalid");
    }
}