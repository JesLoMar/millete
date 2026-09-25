package com.puntomartinez.millete.dashboard.application.services;

import java.time.LocalDate;
import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.BudgetItemResponseDTO;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardBudgetsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardBudgetService {

    private final TransactionQueryPort transactionQueryPort;
    private final CategoryQueryPort categoryQueryPort;
    private final DashboardPeriodService dashboardPeriodService;

    public DashboardBudgetsResponseDTO getBudgets(
            UUID userId,
            String period
    ) {
        LocalDate[] range =
                dashboardPeriodService.getDateRange(period);

        List<CategoryQueryPort.CategoryData> categoriesWithBudget =
                categoryQueryPort.findCategoriesWithBudgetByUserId(userId);

        List<TransactionQueryPort.TransactionData> periodTransactions =
                transactionQueryPort.findByUserIdAndDateBetween(
                        userId,
                        range[0],
                        range[1]
                );

        Map<UUID, List<TransactionQueryPort.TransactionData>>
                transactionsByCategory = periodTransactions.stream()
                .filter(t ->
                        t.categoryId() != null
                                && "EXPENSE".equals(t.type()))
                .collect(Collectors.groupingBy(
                        TransactionQueryPort.TransactionData::categoryId
                ));

        List<BudgetItemResponseDTO> budgetItems =
                categoriesWithBudget.stream()
                        .map(category -> {
                            List<TransactionQueryPort.TransactionData>
                                    categoryTransactions =
                                    transactionsByCategory.getOrDefault(
                                            category.id(),
                                            Collections.emptyList()
                                    );

                            BigDecimal spent = categoryTransactions.stream()
                                    .map(t -> t.amount().abs())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            return new BudgetItemResponseDTO(
                                    category.id(),
                                    category.name(),
                                    spent,
                                    category.budgetLimit(),
                                    calculatePercentage(
                                            spent,
                                            category.budgetLimit()
                                    )
                            );
                        })
                        .sorted((a, b) -> {
                            boolean aOver = a.percentage() >= 100;
                            boolean bOver = b.percentage() >= 100;
                            if (aOver && !bOver) return -1;
                            if (!aOver && bOver) return 1;
                            return Double.compare(
                                    b.percentage(), a.percentage()
                            );
                        })
                        .limit(5)
                        .collect(Collectors.toList());

        return new DashboardBudgetsResponseDTO(period, budgetItems);
    }

    private double calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return part.multiply(new BigDecimal("100"))
                .divide(total, 1, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }
}