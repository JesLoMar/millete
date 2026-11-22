package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardCategoriesResponseDTO(
        BigDecimal totalExpenses,
        List<CategoryExpenseItemResponseDTO> categories,
        BigDecimal totalIncome,
        List<CategoryExpenseItemResponseDTO> incomeCategories
) {
    public DashboardCategoriesResponseDTO(BigDecimal totalExpenses, List<CategoryExpenseItemResponseDTO> categories) {
        this(totalExpenses, categories, BigDecimal.ZERO, List.of());
    }
}
