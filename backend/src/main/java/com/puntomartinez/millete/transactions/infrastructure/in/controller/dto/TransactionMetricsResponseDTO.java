package com.puntomartinez.millete.transactions.infrastructure.in.controller.dto;

import java.math.BigDecimal;

public record TransactionMetricsResponseDTO(
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance,
        BigDecimal transferIn,
        BigDecimal transferOut,
        long transferInCount,
        long transferOutCount,
        long count,
        double incomeTrend,
        double expensesTrend,
        double balanceTrend,
        double countTrend
) {
    public TransactionMetricsResponseDTO(BigDecimal income, BigDecimal expenses, BigDecimal balance,
            BigDecimal transferIn, BigDecimal transferOut, long count,
            double incomeTrend, double expensesTrend, double balanceTrend, double countTrend) {
        this(income, expenses, balance, transferIn, transferOut, 0L, 0L, count,
                incomeTrend, expensesTrend, balanceTrend, countTrend);
    }
}
