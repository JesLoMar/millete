package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.transactions.domain.ports.in.GetTransactionMetricsUseCase;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository.TransactionAggregates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionMetricsService implements GetTransactionMetricsUseCase {

    private final TransactionRepository transactionRepository;
    private final TransactionPeriodService transactionPeriodService;

    @Override
    @Transactional(readOnly = true)
    public MetricsResult getMetrics(MetricsCommand command) {
        LocalDate[] currentRange =
                transactionPeriodService.getDateRange(
                        command.period(), command.userId());

        LocalDate[] previousRange =
                transactionPeriodService.getPreviousPeriod(
                        command.period(), command.userId());

        TransactionAggregates currentAggregates =
                transactionRepository.getAggregatesByUserIdAndDateBetween(
                        command.userId(),
                        currentRange[0],
                        currentRange[1]
                );

        TransactionAggregates previousAggregates =
                transactionRepository.getAggregatesByUserIdAndDateBetween(
                        command.userId(),
                        previousRange[0],
                        previousRange[1]
                );

        BigDecimal currentIncome = currentAggregates.totalIncome();
        BigDecimal currentExpenses = currentAggregates.totalExpense();
        BigDecimal currentBalance = currentIncome.subtract(currentExpenses);
        long currentCount = currentAggregates.count();

        BigDecimal previousIncome = previousAggregates.totalIncome();
        BigDecimal previousExpenses = previousAggregates.totalExpense();
        BigDecimal previousBalance = previousIncome.subtract(previousExpenses);

        double incomeTrend =
                calculateTrend(currentIncome, previousIncome);

        double expensesTrend =
                calculateTrend(currentExpenses, previousExpenses);

        double balanceTrend =
                calculateTrend(currentBalance, previousBalance);

        double countTrend =
                calculateCountTrend(currentCount, previousAggregates.count());

        return new MetricsResult(
                currentIncome,
                currentExpenses,
                currentBalance,
                currentCount,
                incomeTrend,
                expensesTrend,
                balanceTrend,
                countTrend
        );
    }

    private double calculateTrend(
            BigDecimal current,
            BigDecimal previous
    ) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0
                    ? 100.0
                    : 0.0;
        }

        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(
                        previous.abs(),
                        1,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private double calculateCountTrend(
            long current,
            long previous
    ) {
        if (previous == 0) {
            return current > 0
                    ? 100.0
                    : 0.0;
        }

        return Math.round(
                ((double) (current - previous) / previous) * 1000.0
        ) / 10.0;
    }
}
