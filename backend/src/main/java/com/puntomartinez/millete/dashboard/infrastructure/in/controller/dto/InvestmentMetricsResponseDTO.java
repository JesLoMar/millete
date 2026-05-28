package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;

public record InvestmentMetricsResponseDTO(
        BigDecimal portfolioValue,
        BigDecimal monthlyReturn,
        BigDecimal dividends,
        double portfolioTrend,
        double returnTrend,
        double dividendsTrend
) {}