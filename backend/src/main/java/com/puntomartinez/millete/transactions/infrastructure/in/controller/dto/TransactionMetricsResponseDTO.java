package com.puntomartinez.millete.transactions.infrastructure.in.controller.dto;

import java.math.BigDecimal;

public record TransactionMetricsResponseDTO(
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance,
        BigDecimal transferIn,
        BigDecimal transferOut,
        long count,
        double incomeTrend,
        double expensesTrend,
        double balanceTrend,
        double countTrend
) {}
