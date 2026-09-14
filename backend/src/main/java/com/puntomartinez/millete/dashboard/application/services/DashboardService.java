package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.in.GetDashboardDataUseCase;
import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.SavingsGoalQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService implements GetDashboardDataUseCase {

    private final TransactionQueryPort transactionQueryPort;
    private final CategoryQueryPort categoryQueryPort;
    private final SavingsGoalQueryPort savingsGoalQueryPort;
    private final DashboardPeriodService dashboardPeriodService;
    private final DashboardHistoryService dashboardHistoryService;
    private final DashboardCategoryService dashboardCategoryService;
    private final DashboardBudgetService dashboardBudgetService;

    @Override
    public DashboardMetricsResponseDTO getMetrics(
            UUID userId,
            String period
    ) {
        LocalDateTime[] currentRange =
                dashboardPeriodService.getDateRange(period);

        LocalDateTime[] previousRange =
                dashboardPeriodService.getPreviousPeriod(period);

        List<TransactionQueryPort.TransactionData> currentTransactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        currentRange[0],
                        currentRange[1]
                );

        List<TransactionQueryPort.TransactionData> previousTransactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        previousRange[0],
                        previousRange[1]
                );

        BigDecimal currentIncome =
                sumByType(currentTransactions, "INCOME");

        BigDecimal currentExpenses =
                sumByType(currentTransactions, "EXPENSE");

        BigDecimal currentBalance =
                currentIncome.subtract(currentExpenses);

        BigDecimal previousIncome =
                sumByType(previousTransactions, "INCOME");

        BigDecimal previousExpenses =
                sumByType(previousTransactions, "EXPENSE");

        BigDecimal previousBalance =
                previousIncome.subtract(previousExpenses);

        return new DashboardMetricsResponseDTO(
                currentBalance,
                currentIncome,
                currentExpenses,
                currentIncome.subtract(currentExpenses),
                calculateTrend(
                        currentBalance,
                        previousBalance
                ),
                calculateTrend(
                        currentIncome,
                        previousIncome
                ),
                calculateTrend(
                        currentExpenses,
                        previousExpenses
                ),
                calculateTrend(
                        currentIncome.subtract(currentExpenses),
                        previousBalance
                )
        );
    }

    @Override
    public DashboardHistoryResponseDTO getHistory(
            UUID userId,
            String period
    ) {
        return dashboardHistoryService.getHistory(
                userId,
                period
        );
    }

    @Override
    public DashboardCategoriesResponseDTO getCategories(
            UUID userId,
            String period
    ) {
        return dashboardCategoryService.getCategories(
                userId,
                period
        );
    }

    @Override
    public DashboardBudgetsResponseDTO getBudgets(
            UUID userId,
            String period
    ) {
        return dashboardBudgetService.getBudgets(
                userId,
                period
        );
    }

    @Override
    public DashboardTransactionsResponseDTO getRecentTransactions(
            UUID userId,
            int limit
    ) {
        List<TransactionQueryPort.TransactionData> recentTransactions =
                transactionQueryPort.findRecentByUserId(
                        userId,
                        limit
                );

        List<UUID> categoryIds =
                recentTransactions.stream()
                        .map(TransactionQueryPort.TransactionData::categoryId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<UUID, CategoryQueryPort.CategoryData> categoriesById =
                categoryIds.isEmpty()
                        ? Collections.emptyMap()
                        : categoryQueryPort.findByIdsAndUserId(
                                        userId,
                                        categoryIds
                                )
                                .stream()
                                .collect(Collectors.toMap(
                                        CategoryQueryPort.CategoryData::id,
                                        category -> category
                                ));

        List<RecentTransactionResponseDTO> transactionDTOs =
                recentTransactions.stream()
                        .map(t -> {
                            String catName = "Sin categoría";
                            String catColor = null;

                            if (t.categoryId() != null) {
                                CategoryQueryPort.CategoryData category =
                                        categoriesById.get(t.categoryId());

                                if (category != null) {
                                    catName = category.name();
                                    catColor = category.color();
                                }
                            }

                            return new RecentTransactionResponseDTO(
                                    t.id(),
                                    t.description(),
                                    catName,
                                    catColor,
                                    t.categoryId(),
                                    t.amount(),
                                    t.date(),
                                    t.type()
                            );
                        })
                        .collect(Collectors.toList());

        return new DashboardTransactionsResponseDTO(
                transactionDTOs
        );
    }

    @Override
    public DashboardGoalsResponseDTO getSavingsGoals(
            UUID userId
    ) {
        List<SavingsGoalQueryPort.SavingsGoalData> goals =
                savingsGoalQueryPort.findAllByUserId(userId);

        List<SavingsGoalResponseDTO> goalDTOs =
                goals.stream()
                        .sorted((a, b) -> {

                            int priorityCompare =
                                    comparePriority(
                                            b.priority(),
                                            a.priority()
                                    );

                            if (priorityCompare != 0) {
                                return priorityCompare;
                            }

                            return b.createdAt()
                                    .compareTo(a.createdAt());
                        })
                        .map(goal ->
                                new SavingsGoalResponseDTO(
                                        goal.id(),
                                        goal.name(),
                                        goal.currentAmount(),
                                        goal.targetAmount(),
                                        calculatePercentage(
                                                goal.currentAmount(),
                                                goal.targetAmount()
                                        ),
                                        mapPriorityToIcon(
                                                goal.priority()
                                        ),
                                        goal.deadline()
                                )
                        )
                        .toList();

        return new DashboardGoalsResponseDTO(
                goalDTOs
        );
    }

    private BigDecimal sumByType(
            List<TransactionQueryPort.TransactionData> transactions,
            String type
    ) {
        BigDecimal sum = BigDecimal.ZERO;

        for (TransactionQueryPort.TransactionData t : transactions) {
            if (type.equals(t.type())) {
                sum = sum.add(
                        t.amount().abs()
                );
            }
        }

        return sum;
    }

    private double calculateTrend(
            BigDecimal current,
            BigDecimal previous
    ) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0
                    ? 100.0
                    : 0.0;
        }

        return current
                .subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(
                        previous.abs(),
                        1,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private double calculatePercentage(
            BigDecimal part,
            BigDecimal total
    ) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return part
                .multiply(new BigDecimal("100"))
                .divide(
                        total,
                        1,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private int comparePriority(
            String a,
            String b
    ) {
        Map<String, Integer> priorityOrder =
                Map.of(
                        "HIGH", 3,
                        "MEDIUM", 2,
                        "LOW", 1
                );

        return Integer.compare(
                priorityOrder.getOrDefault(a, 0),
                priorityOrder.getOrDefault(b, 0)
        );
    }

    private String mapPriorityToIcon(String priority) {
        return switch (priority) {
            case "HIGH" -> "high";
            case "MEDIUM" -> "medium";
            case "LOW" -> "low";
            default -> "default";
        };
    }
}