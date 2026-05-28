package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;

public record DashboardMetricsResponseDTO(
        BigDecimal balance,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal savings,
        double balanceTrend,
        double incomeTrend,
        double expensesTrend,
        double savingsTrend
) {}