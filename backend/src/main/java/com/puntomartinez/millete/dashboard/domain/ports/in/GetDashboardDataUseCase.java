package com.puntomartinez.millete.dashboard.domain.ports.in;

import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.*;

import java.util.UUID;

public interface GetDashboardDataUseCase {
    DashboardMetricsResponseDTO getMetrics(UUID userId, String period);
    DashboardHistoryResponseDTO getHistory(UUID userId, String period);
    DashboardCategoriesResponseDTO getCategories(UUID userId, String period);
    DashboardBudgetsResponseDTO getBudgets(UUID userId, String period);
    DashboardTransactionsResponseDTO getRecentTransactions(UUID userId, int limit);
    DashboardGoalsResponseDTO getSavingsGoals(UUID userId);
    InvestmentMetricsResponseDTO getInvestmentMetrics(UUID userId, String period);
    InvestmentEvolutionResponseDTO getInvestmentEvolution(UUID userId, String period);
    InvestmentDistributionResponseDTO getInvestmentDistribution(UUID userId, String period);
}