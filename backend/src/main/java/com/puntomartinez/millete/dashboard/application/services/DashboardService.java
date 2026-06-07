package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dashboard.domain.ports.in.GetDashboardDataUseCase;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService implements GetDashboardDataUseCase {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final InvestmentRepository investmentRepository;

    private static final String[] CHART_COLORS = {
            "hsl(var(--chart-1))", "hsl(var(--chart-2))", "hsl(var(--chart-3))",
            "hsl(var(--chart-4))", "hsl(var(--chart-5))"
    };

    // =====================================================
    // 1. MÉTRICAS DEL DASHBOARD
    // =====================================================
    @Override
    public DashboardMetricsResponseDTO getMetrics(UUID userId, String period) {
        LocalDateTime[] currentRange = getDateRange(period);
        LocalDateTime[] previousRange = getPreviousPeriod(period);

        List<Transaction> currentTransactions = transactionRepository
                .findByUserIdAndDateBetween(userId, currentRange[0], currentRange[1]);

        List<Transaction> previousTransactions = transactionRepository
                .findByUserIdAndDateBetween(userId, previousRange[0], previousRange[1]);

        BigDecimal currentIncome = sumByType(currentTransactions, TransactionType.INCOME);
        BigDecimal currentExpenses = sumByType(currentTransactions, TransactionType.EXPENSE);
        BigDecimal currentBalance = currentIncome.subtract(currentExpenses);

        BigDecimal previousIncome = sumByType(previousTransactions, TransactionType.INCOME);
        BigDecimal previousExpenses = sumByType(previousTransactions, TransactionType.EXPENSE);
        BigDecimal previousBalance = previousIncome.subtract(previousExpenses);

        return new DashboardMetricsResponseDTO(
                currentBalance,
                currentIncome,
                currentExpenses,
                currentIncome.subtract(currentExpenses),
                calculateTrend(currentBalance, previousBalance),
                calculateTrend(currentIncome, previousIncome),
                calculateTrend(currentExpenses, previousExpenses),
                calculateTrend(currentIncome.subtract(currentExpenses), previousIncome.subtract(previousExpenses)
                ));
    }

    // =====================================================
    // 2. HISTÓRICO DE GASTOS
    // =====================================================
    @Override
    public DashboardHistoryResponseDTO getHistory(UUID userId, String period) {
        return switch (period.toLowerCase()) {
            case "week" -> getWeeklyHistory(userId);
            case "month" -> getMonthlyHistory(userId);
            case "year" -> getYearlyHistory(userId);
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    // =====================================================
    // 3. GASTOS POR CATEGORÍA
    // =====================================================
    @Override
    public DashboardCategoriesResponseDTO getCategories(UUID userId, String period) {
        LocalDateTime[] range = getDateRange(period);

        List<Transaction> expenses = transactionRepository
                .findByUserIdAndDateBetween(userId, range[0], range[1])
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .toList();

        BigDecimal totalExpenses = expenses.stream()
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, List<Transaction>> byCategory = new HashMap<>();
        List<Transaction> orphanTransactions = new ArrayList<>();

        for (Transaction tx : expenses) {
            if (tx.getCategoryId() == null) {
                orphanTransactions.add(tx);
            } else {
                byCategory.computeIfAbsent(tx.getCategoryId(), k -> new ArrayList<>()).add(tx);
            }
        }

        List<CategoryExpenseItemResponseDTO> categoryItems = new ArrayList<>();

        // Procesar categorías con ID
        for (Map.Entry<UUID, List<Transaction>> entry : byCategory.entrySet()) {
            UUID categoryId = entry.getKey();
            BigDecimal amount = entry.getValue().stream()
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            double percentage = calculatePercentage(amount, totalExpenses);

            String categoryName = categoryRepository.findById(categoryId)
                    .map(Category::getName)
                    .orElse("Sin categoría");

            categoryItems.add(new CategoryExpenseItemResponseDTO(
                    categoryName, amount, percentage, entry.getValue().size()
            ));
        }

        // Procesar transacciones huérfanas (sin categoría)
        if (!orphanTransactions.isEmpty()) {
            BigDecimal orphanAmount = orphanTransactions.stream()
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            double orphanPercentage = calculatePercentage(orphanAmount, totalExpenses);

            // Combinar con "Sin categoría" existente o crear nuevo
            Optional<CategoryExpenseItemResponseDTO> existingOrphan = categoryItems.stream()
                    .filter(item -> "Sin categoría".equals(item.name()))
                    .findFirst();

            if (existingOrphan.isPresent()) {
                CategoryExpenseItemResponseDTO old = existingOrphan.get();
                BigDecimal combinedAmount = old.amount().add(orphanAmount);
                double combinedPercentage = calculatePercentage(combinedAmount, totalExpenses);
                int combinedCount = old.transactionCount() + orphanTransactions.size();

                categoryItems.remove(old);
                categoryItems.add(new CategoryExpenseItemResponseDTO(
                        "Sin categoría", combinedAmount, combinedPercentage, combinedCount
                ));
            } else {
                categoryItems.add(new CategoryExpenseItemResponseDTO(
                        "Sin categoría", orphanAmount, orphanPercentage, orphanTransactions.size()
                ));
            }
        }

        categoryItems.sort((a, b) -> b.amount().compareTo(a.amount()));

        return new DashboardCategoriesResponseDTO(totalExpenses, groupSmallCategories(categoryItems, totalExpenses));
    }

    // =====================================================
    // 4. PRESUPUESTOS
    // =====================================================
    @Override
    public DashboardBudgetsResponseDTO getBudgets(UUID userId, String period) {
        LocalDateTime[] range = getDateRange(period);

        List<Category> categoriesWithBudget = categoryRepository.findCategoriesWithBudgetByUserId(userId);

        List<Transaction> periodTransactions = transactionRepository
                .findByUserIdAndDateBetween(userId, range[0], range[1]);

        List<BudgetItemResponseDTO> budgetItems = categoriesWithBudget.stream()
                .map(category -> {
                    BigDecimal spent = periodTransactions.stream()
                            .filter(t -> t.getCategoryId() != null && t.getCategoryId().equals(category.getId()))
                            .filter(t -> t.getType() == TransactionType.EXPENSE)
                            .map(t -> t.getAmount().abs())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new BudgetItemResponseDTO(category.getId(), category.getName(), spent, category.getBudgetLimit(),
                            calculatePercentage(spent, category.getBudgetLimit()));
                })
                .filter(b -> b.spent().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> {
                    boolean aOver = a.percentage() >= 100;
                    boolean bOver = b.percentage() >= 100;
                    if (aOver && !bOver) return -1;
                    if (!aOver && bOver) return 1;
                    return Double.compare(b.percentage(), a.percentage());
                })
                .collect(Collectors.toList());

        return new DashboardBudgetsResponseDTO(period, budgetItems);
    }

    // =====================================================
    // 5. TRANSACCIONES RECIENTES
    // =====================================================
    @Override
    public DashboardTransactionsResponseDTO getRecentTransactions(UUID userId, int limit) {
        List<Transaction> recentTransactions = transactionRepository.findRecentByUserId(userId, limit);

        List<RecentTransactionResponseDTO> transactionDTOs = recentTransactions.stream()
                .map(t -> {
                    String catName = "Sin categoría";
                    String catColor = null;
                    if (t.getCategoryId() != null) {
                        var cat = categoryRepository.findById(t.getCategoryId());
                        if (cat.isPresent()) {
                            catName = cat.get().getName();
                            catColor = cat.get().getColor();
                        }
                    }
                    return new RecentTransactionResponseDTO(
                            t.getId(), t.getDescription(), catName, catColor,
                            t.getCategoryId(), t.getAmount(), t.getDate(), t.getType().name()
                    );
                })
                .collect(Collectors.toList());
        return new DashboardTransactionsResponseDTO(transactionDTOs);
    }

    // =====================================================
    // 6. METAS DE AHORRO
    // =====================================================
    @Override
    public DashboardGoalsResponseDTO getSavingsGoals(UUID userId) {
        LocalDateTime yearAgo = LocalDateTime.now().minusYears(1);
        List<Transaction> yearlyTransactions = transactionRepository
                .findByUserIdAndDateBetween(userId, yearAgo, LocalDateTime.now());
        return new DashboardGoalsResponseDTO(generateSavingsGoals(yearlyTransactions, userId));
    }

    // =====================================================
    // 7. MÉTRICAS DE INVERSIONES
    // =====================================================
    @Override
    public InvestmentMetricsResponseDTO getInvestmentMetrics(UUID userId, String period) {
        List<Investment> currentInvestments = investmentRepository.findAllByUserId(userId).stream()
                .filter(Investment::isActive).toList();

        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal currentReturn = BigDecimal.ZERO;
        BigDecimal investedCapital = BigDecimal.ZERO;

        for (Investment inv : currentInvestments) {
            currentValue = currentValue.add(inv.getCurrentValue());
            currentReturn = currentReturn.add(inv.getProfitOrLoss());
            investedCapital = investedCapital.add(inv.getInvestedCapital());
        }

        double returnTrend = investedCapital.compareTo(BigDecimal.ZERO) > 0
                ? currentReturn.divide(investedCapital, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).doubleValue()
                : 0.0;

        return new InvestmentMetricsResponseDTO(currentValue, currentReturn, BigDecimal.ZERO,
                calculateTrend(currentValue, investedCapital), returnTrend, 0.0);
    }

    // =====================================================
    // 8. EVOLUCIÓN DEL PATRIMONIO
    // =====================================================
    @Override
    public InvestmentEvolutionResponseDTO getInvestmentEvolution(UUID userId, String period) {
        return switch (period.toLowerCase()) {
            case "week" -> getWeeklyInvestmentEvolution(userId);
            case "month" -> getMonthlyInvestmentEvolution(userId);
            case "year" -> getYearlyInvestmentEvolution(userId);
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    private InvestmentEvolutionResponseDTO getWeeklyInvestmentEvolution(UUID userId) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        String[] dayNames = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            if (day.isAfter(today)) break;
            labels.add(dayNames[i]);
            data.add(getPortfolioValueAtDate(userId, day.atTime(23, 59, 59)));
        }
        return new InvestmentEvolutionResponseDTO("week", labels, data);
    }

    private InvestmentEvolutionResponseDTO getMonthlyInvestmentEvolution(UUID userId) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM", Locale.of("es"));

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            labels.add(monthStart.format(fmt));
            data.add(getPortfolioValueAtDate(userId, monthStart.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)));
        }
        return new InvestmentEvolutionResponseDTO("month", labels, data);
    }

    private InvestmentEvolutionResponseDTO getYearlyInvestmentEvolution(UUID userId) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        String[] monthNames = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        LocalDate today = LocalDate.now();

        for (int m = 1; m <= 12; m++) {
            LocalDate monthStart = LocalDate.of(today.getYear(), m, 1);
            if (monthStart.isAfter(today)) break;
            labels.add(monthNames[m - 1]);
            data.add(getPortfolioValueAtDate(userId, monthStart.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)));
        }
        return new InvestmentEvolutionResponseDTO("year", labels, data);
    }

    private BigDecimal getPortfolioValueAtDate(UUID userId, LocalDateTime date) {
        BigDecimal total = BigDecimal.ZERO;
        List<Investment> investments = investmentRepository.findAllByUserId(userId);
        for (Investment inv : investments) {
            if (inv.getPurchaseDate() != null && inv.getPurchaseDate().isBefore(date) && inv.isActive()) {
                total = total.add(inv.getCurrentValue());
            }
        }
        return total;
    }

    // =====================================================
    // 9. DISTRIBUCIÓN DE CARTERA
    // =====================================================
    @Override
    public InvestmentDistributionResponseDTO getInvestmentDistribution(UUID userId, String period) {
        List<Investment> investments = investmentRepository.findAllByUserId(userId).stream()
                .filter(Investment::isActive).toList();

        BigDecimal totalValue = BigDecimal.ZERO;
        for (Investment inv : investments) {
            totalValue = totalValue.add(inv.getCurrentValue());
        }

        Map<String, List<Investment>> byType = investments.stream()
                .collect(Collectors.groupingBy(i -> i.getType().name()));

        List<InvestmentDistributionItemDTO> distribution = new ArrayList<>();
        int colorIndex = 0;

        for (Map.Entry<String, List<Investment>> entry : byType.entrySet()) {
            BigDecimal typeValue = BigDecimal.ZERO;
            for (Investment inv : entry.getValue()) {
                typeValue = typeValue.add(inv.getCurrentValue());
            }
            distribution.add(new InvestmentDistributionItemDTO(entry.getKey(), typeValue,
                    calculatePercentage(typeValue, totalValue), CHART_COLORS[colorIndex % CHART_COLORS.length]));
            colorIndex++;
        }

        return new InvestmentDistributionResponseDTO(totalValue, distribution);
    }

    // =====================================================
    // MÉTODOS PRIVADOS AUXILIARES
    // =====================================================

    private LocalDateTime[] getDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toLowerCase()) {
            case "week" -> new LocalDateTime[]{
                    now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0),
                    now.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59)
            };
            case "month" -> new LocalDateTime[]{
                    now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                    now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59)
            };
            case "year" -> new LocalDateTime[]{
                    now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0),
                    now.with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59)
            };
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    private LocalDateTime[] getPreviousPeriod(String period) {
        LocalDateTime[] currentRange = getDateRange(period);
        LocalDateTime previousStart;
        LocalDateTime previousEnd = switch (period.toLowerCase()) {
            case "week" -> {
                previousStart = currentRange[0].minusWeeks(1);
                yield currentRange[1].minusWeeks(1);
            }
            case "month" -> {
                previousStart = currentRange[0].minusMonths(1);
                yield currentRange[0].minusDays(1).withHour(23).withMinute(59).withSecond(59);
            }
            case "year" -> {
                previousStart = currentRange[0].minusYears(1);
                yield currentRange[0].minusDays(1).withHour(23).withMinute(59).withSecond(59);
            }
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

        return new LocalDateTime[]{previousStart, previousEnd};
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getType() == type) {
                sum = sum.add(t.getAmount().abs());
            }
        }
        return sum;
    }

    private double calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous.abs(), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return part.multiply(new BigDecimal("100"))
                .divide(total, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private DashboardHistoryResponseDTO getWeeklyHistory(UUID userId) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        String[] dayNames = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            if (day.isAfter(today)) break;

            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = day.atTime(23, 59, 59);

            BigDecimal dayExpenses = transactionRepository
                    .findByUserIdAndDateBetween(userId, dayStart, dayEnd)
                    .stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labels.add(dayNames[i]);
            data.add(dayExpenses);
        }

        return new DashboardHistoryResponseDTO("week", labels, data);
    }

    private DashboardHistoryResponseDTO getMonthlyHistory(UUID userId) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        int weekNumber = 1;
        LocalDate weekStart = monthStart;

        while (weekStart.isBefore(today.plusDays(1)) && weekNumber <= 5) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(today)) {
                weekEnd = today;
            }

            LocalDateTime startDateTime = weekStart.atStartOfDay();
            LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);

            BigDecimal weekExpenses = transactionRepository
                    .findByUserIdAndDateBetween(userId, startDateTime, endDateTime)
                    .stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labels.add("Sem " + weekNumber);
            data.add(weekExpenses);

            weekStart = weekStart.plusWeeks(1);
            weekNumber++;
        }

        return new DashboardHistoryResponseDTO("month", labels, data);
    }

    private DashboardHistoryResponseDTO getYearlyHistory(UUID userId) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        String[] monthNames = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(currentYear, month, 1);
            if (monthStart.isAfter(today)) break;

            LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
            if (monthEnd.isAfter(today)) {
                monthEnd = today;
            }

            LocalDateTime startDateTime = monthStart.atStartOfDay();
            LocalDateTime endDateTime = monthEnd.atTime(23, 59, 59);

            BigDecimal monthExpenses = transactionRepository
                    .findByUserIdAndDateBetween(userId, startDateTime, endDateTime)
                    .stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labels.add(monthNames[month - 1]);
            data.add(monthExpenses);
        }

        return new DashboardHistoryResponseDTO("year", labels, data);
    }

    private List<CategoryExpenseItemResponseDTO> groupSmallCategories(
            List<CategoryExpenseItemResponseDTO> categories,
            BigDecimal totalExpenses) {

        List<CategoryExpenseItemResponseDTO> mainCategories = new ArrayList<>();
        BigDecimal othersAmount = BigDecimal.ZERO;
        int othersCount = 0;

        for (CategoryExpenseItemResponseDTO item : categories) {
            if (item.percentage() < 5.0) {
                othersAmount = othersAmount.add(item.amount());
                othersCount += item.transactionCount();
            } else {
                mainCategories.add(item);
            }
        }

        if (othersAmount.compareTo(BigDecimal.ZERO) > 0) {
            double othersPercentage = calculatePercentage(othersAmount, totalExpenses);
            mainCategories.add(new CategoryExpenseItemResponseDTO(
                    "Otros", othersAmount, othersPercentage, othersCount
            ));
        }

        return mainCategories;
    }

    private List<SavingsGoalResponseDTO> generateSavingsGoals(List<Transaction> yearlyTransactions, UUID userId) {
        List<SavingsGoalResponseDTO> goals = new ArrayList<>();

        BigDecimal totalIncome = sumByType(yearlyTransactions, TransactionType.INCOME);
        BigDecimal monthlyAverageIncome = totalIncome.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

        BigDecimal totalExpenses = sumByType(yearlyTransactions, TransactionType.EXPENSE);
        BigDecimal currentSavings = totalIncome.subtract(totalExpenses);

        BigDecimal emergencyTarget = monthlyAverageIncome.multiply(new BigDecimal("3"));
        goals.add(new SavingsGoalResponseDTO(
                UUID.randomUUID(),
                "Fondo de Emergencia",
                currentSavings.min(emergencyTarget),
                emergencyTarget,
                calculatePercentage(currentSavings.min(emergencyTarget), emergencyTarget),
                "default",
                LocalDate.now().plusMonths(6)
        ));

        BigDecimal vacationTarget = monthlyAverageIncome.multiply(new BigDecimal("0.2")).multiply(new BigDecimal("6"));
        goals.add(new SavingsGoalResponseDTO(
                UUID.randomUUID(),
                "Vacaciones",
                currentSavings.multiply(new BigDecimal("0.3")),
                vacationTarget,
                calculatePercentage(currentSavings.multiply(new BigDecimal("0.3")), vacationTarget),
                "vacation",
                LocalDate.now().plusMonths(6)
        ));

        return goals;
    }
}