package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.in.GetDashboardDataUseCase;
import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.SavingsGoalQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio principal del dashboard.
 *
 * <p>CÁLCULOS EN MEMORIA (deuda técnica documentada):
 * Varios métodos cargan transacciones del periodo y calculan métricas
 * en memoria Java. Esto es aceptable para el volumen de datos de un
 * usuario individual en self-hosting, pero si el volumen crece se
 * debería valorar:</p>
 * <ul>
 *   <li>Agregaciones en base de datos (GROUP BY, SUM)</li>
 *   <li>Vistas materializadas de PostgreSQL</li>
 *   <li>Caché por usuario/periodo</li>
 * </ul>
 *
 * <p>CACHÉ: No hay caché implementada. El dashboard es una de las
 * zonas más leídas de la aplicación. Si el rendimiento lo requiere,
 * valorar caché con Spring Cache o Redis por (userId, period).</p>
 *
 * <p>REDUNDANCIAS EN DashboardMetricsResponseDTO (documentadas):</p>
 * <ul>
 *   <li>{@code savings} es idéntico a {@code balance} — el frontend
 *       consume ambos campos por compatibilidad histórica.</li>
 *   <li>{@code savingsTrend} se calcula con los mismos valores que
 *       {@code balanceTrend} — alias semántico para el frontend.</li>
 * </ul>
 * <p>Estos campos se mantienen hasta que se revise el frontend y
 * se pueda eliminar la redundancia de forma coordinada.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService implements GetDashboardDataUseCase {

    private static final int MAX_SAVINGS_GOALS = 20;

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
        LocalDate[] currentRange =
                dashboardPeriodService.getDateRange(period, userId);
        LocalDate[] previousRange =
                dashboardPeriodService.getPreviousPeriod(period, userId);

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

        double balanceTrend = calculateTrend(
                currentBalance,
                previousBalance
        );

        return new DashboardMetricsResponseDTO(
                currentBalance,
                currentIncome,
                currentExpenses,
                currentBalance,
                balanceTrend,
                calculateTrend(currentIncome, previousIncome),
                calculateTrend(currentExpenses, previousExpenses),
                balanceTrend
        );
    }

    @Override
    public DashboardHistoryResponseDTO getHistory(
            UUID userId,
            String period
    ) {
        return dashboardHistoryService.getHistory(userId, period);
    }

    @Override
    public DashboardCategoriesResponseDTO getCategories(
            UUID userId,
            String period
    ) {
        return dashboardCategoryService.getCategories(userId, period);
    }

    @Override
    public DashboardBudgetsResponseDTO getBudgets(
            UUID userId,
            String period
    ) {
        return dashboardBudgetService.getBudgets(userId, period);
    }

    @Override
    public DashboardTransactionsResponseDTO getRecentTransactions(
            UUID userId,
            int limit
    ) {
        List<TransactionQueryPort.TransactionData> recentTransactions =
                transactionQueryPort.findRecentByUserId(userId, limit);

        List<UUID> categoryIds = recentTransactions.stream()
                .map(TransactionQueryPort.TransactionData::categoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, CategoryQueryPort.CategoryData> categoriesById =
                categoryIds.isEmpty()
                        ? Collections.emptyMap()
                        : categoryQueryPort
                        .findByIdsAndUserId(userId, categoryIds)
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

        return new DashboardTransactionsResponseDTO(transactionDTOs);
    }

    @Override
    public DashboardGoalsResponseDTO getSavingsGoals(UUID userId) {
        List<SavingsGoalResponseDTO> goalDTOs =
                savingsGoalQueryPort.findAllByUserId(userId)
                        .stream()
                        .sorted((a, b) -> {
                            int priorityCompare = comparePriority(
                                    b.priority(),
                                    a.priority()
                            );
                            if (priorityCompare != 0) {
                                return priorityCompare;
                            }
                            return b.createdAt().compareTo(a.createdAt());
                        })
                        .limit(MAX_SAVINGS_GOALS)
                        .map(goal -> new SavingsGoalResponseDTO(
                                goal.id(),
                                goal.name(),
                                goal.currentAmount(),
                                goal.targetAmount(),
                                calculatePercentage(
                                        goal.currentAmount(),
                                        goal.targetAmount()
                                ),
                                mapPriorityToIcon(goal.priority()),
                                goal.deadline()
                        ))
                        .toList();

        return new DashboardGoalsResponseDTO(goalDTOs);
    }

    private BigDecimal sumByType(
            List<TransactionQueryPort.TransactionData> transactions,
            String type
    ) {
        BigDecimal sum = BigDecimal.ZERO;
        for (TransactionQueryPort.TransactionData t : transactions) {
            if (type.equals(t.type())) {
                sum = sum.add(t.amount().abs());
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
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return part.multiply(new BigDecimal("100"))
                .divide(total, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private int comparePriority(String a, String b) {
        Map<String, Integer> priorityOrder = Map.of(
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
