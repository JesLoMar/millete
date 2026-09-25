package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.CategoryExpenseItemResponseDTO;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.DashboardCategoriesResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardCategoryService {

    private final TransactionQueryPort transactionQueryPort;
    private final CategoryQueryPort categoryQueryPort;
    private final DashboardPeriodService dashboardPeriodService;

    public DashboardCategoriesResponseDTO getCategories(
            UUID userId,
            String period
    ) {
        LocalDate[] range =
                dashboardPeriodService.getDateRange(period);

        List<TransactionQueryPort.TransactionData> expenses =
                transactionQueryPort.findByUserIdAndDateBetween(
                                userId,
                                range[0],
                                range[1]
                        )
                        .stream()
                        .filter(t -> "EXPENSE".equals(t.type()))
                        .toList();

        BigDecimal totalExpenses = BigDecimal.ZERO;
        Map<UUID, BigDecimal> amountByCategory = new HashMap<>();
        Map<UUID, Integer> countByCategory = new HashMap<>();
        BigDecimal orphanAmount = BigDecimal.ZERO;
        int orphanCount = 0;

        for (TransactionQueryPort.TransactionData tx : expenses) {
            BigDecimal amount = tx.amount().abs();
            totalExpenses = totalExpenses.add(amount);

            if (tx.categoryId() == null) {
                orphanAmount = orphanAmount.add(amount);
                orphanCount++;
                continue;
            }

            amountByCategory.merge(
                    tx.categoryId(),
                    amount,
                    BigDecimal::add
            );

            countByCategory.merge(
                    tx.categoryId(),
                    1,
                    Integer::sum
            );
        }

        Map<UUID, CategoryQueryPort.CategoryData> categoriesById =
                categoryQueryPort.findByUserId(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                CategoryQueryPort.CategoryData::id,
                                category -> category
                        ));

        List<CategoryExpenseItemResponseDTO> categoryItems =
                new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> entry :
                amountByCategory.entrySet()) {

            UUID categoryId = entry.getKey();
            BigDecimal amount = entry.getValue();

            CategoryQueryPort.CategoryData category =
                    categoriesById.get(categoryId);

            if (category == null) {
                orphanAmount = orphanAmount.add(amount);
                orphanCount += countByCategory.getOrDefault(
                        categoryId,
                        0
                );
                continue;
            }

            categoryItems.add(
                    new CategoryExpenseItemResponseDTO(
                            category.id(),
                            category.name(),
                            amount,
                            calculatePercentage(
                                    amount,
                                    totalExpenses
                            ),
                            countByCategory.getOrDefault(
                                    categoryId,
                                    0
                            )
                    )
            );
        }

        if (orphanAmount.compareTo(BigDecimal.ZERO) > 0) {
            categoryItems.add(
                    new CategoryExpenseItemResponseDTO(
                            null,
                            "Sin categoría",
                            orphanAmount,
                            calculatePercentage(
                                    orphanAmount,
                                    totalExpenses
                            ),
                            orphanCount
                    )
            );
        }

        categoryItems.sort(
                (a, b) -> b.amount().compareTo(a.amount())
        );

        return new DashboardCategoriesResponseDTO(
                totalExpenses,
                groupSmallCategories(
                        categoryItems,
                        totalExpenses
                )
        );
    }

    private double calculatePercentage(
            BigDecimal part,
            BigDecimal total
    ) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return part.multiply(new BigDecimal("100"))
                .divide(
                        total,
                        1,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private List<CategoryExpenseItemResponseDTO> groupSmallCategories(
            List<CategoryExpenseItemResponseDTO> categories,
            BigDecimal totalExpenses
    ) {
        List<CategoryExpenseItemResponseDTO> mainCategories =
                new ArrayList<>();

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
            double othersPercentage = calculatePercentage(
                    othersAmount,
                    totalExpenses
            );

            mainCategories.add(
                    new CategoryExpenseItemResponseDTO(
                            null,
                            "Otros",
                            othersAmount,
                            othersPercentage,
                            othersCount
                    )
            );
        }

        return mainCategories;
    }
}