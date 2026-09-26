package com.puntomartinez.millete.transactions.domain.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface GetTransactionMetricsUseCase {

    record MetricsCommand(
            UUID userId,
            String period
    ) {
    }

    record MetricsResult(
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
    ) {
    }

    MetricsResult getMetrics(MetricsCommand command);
}
