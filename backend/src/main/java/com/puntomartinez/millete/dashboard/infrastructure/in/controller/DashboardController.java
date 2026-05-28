package com.puntomartinez.millete.dashboard.infrastructure.in.controller;

import com.puntomartinez.millete.dashboard.domain.ports.in.GetDashboardDataUseCase;
import com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetDashboardDataUseCase getDashboardDataUseCase;

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsResponseDTO> getMetrics(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getMetrics(getUserId(authentication), period));
    }

    @GetMapping("/history")
    public ResponseEntity<DashboardHistoryResponseDTO> getHistory(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getHistory(getUserId(authentication), period));
    }

    @GetMapping("/categories")
    public ResponseEntity<DashboardCategoriesResponseDTO> getCategories(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getCategories(getUserId(authentication), period));
    }

    @GetMapping("/budgets")
    public ResponseEntity<DashboardBudgetsResponseDTO> getBudgets(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getBudgets(getUserId(authentication), period));
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<DashboardTransactionsResponseDTO> getRecentTransactions(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getRecentTransactions(getUserId(authentication), limit));
    }

    @GetMapping("/savings-goals")
    public ResponseEntity<DashboardGoalsResponseDTO> getSavingsGoals(Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getSavingsGoals(getUserId(authentication)));
    }

    @GetMapping("/investments/metrics")
    public ResponseEntity<InvestmentMetricsResponseDTO> getInvestmentMetrics(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getInvestmentMetrics(getUserId(authentication), period));
    }

    @GetMapping("/investments/evolution")
    public ResponseEntity<InvestmentEvolutionResponseDTO> getInvestmentEvolution(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getInvestmentEvolution(getUserId(authentication), period));
    }

    @GetMapping("/investments/distribution")
    public ResponseEntity<InvestmentDistributionResponseDTO> getInvestmentDistribution(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        return ResponseEntity.ok(getDashboardDataUseCase.getInvestmentDistribution(getUserId(authentication), period));
    }
}