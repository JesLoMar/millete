package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.util.List;

public record DashboardBudgetsResponseDTO(
        String period,
        List<BudgetItemResponseDTO> budgets
) {}