package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import java.math.BigDecimal;

public record InvestmentMetricsResponseDTO(
        BigDecimal portfolioValue,
        BigDecimal monthlyReturn,
        BigDecimal dividends,
        double portfolioTrend,
        double returnTrend,
        double dividendsTrend
) {}
