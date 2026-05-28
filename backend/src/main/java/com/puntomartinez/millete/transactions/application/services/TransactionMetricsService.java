package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.in.GetTransactionMetricsUseCase;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionMetricsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionMetricsService implements GetTransactionMetricsUseCase {

    private final TransactionRepository transactionRepository;

    @Override
    public TransactionMetricsResponseDTO getMetrics(MetricsCommand command) {
        LocalDateTime[] currentRange = getDateRange(command.period());
        LocalDateTime[] previousRange = getPreviousPeriod(command.period());

        List<Transaction> currentTransactions = transactionRepository
                .findByUserIdAndDateBetween(command.userId(), currentRange[0], currentRange[1])
                .stream()
                .filter(Transaction::isActive)
                .toList();

        List<Transaction> previousTransactions = transactionRepository
                .findByUserIdAndDateBetween(command.userId(), previousRange[0], previousRange[1])
                .stream()
                .filter(Transaction::isActive)
                .toList();

        BigDecimal currentIncome = sumByType(currentTransactions, TransactionType.INCOME);
        BigDecimal currentExpenses = sumByType(currentTransactions, TransactionType.EXPENSE);
        BigDecimal currentBalance = currentIncome.subtract(currentExpenses);
        long currentCount = currentTransactions.size();

        BigDecimal previousIncome = sumByType(previousTransactions, TransactionType.INCOME);
        BigDecimal previousExpenses = sumByType(previousTransactions, TransactionType.EXPENSE);
        BigDecimal previousBalance = previousIncome.subtract(previousExpenses);
        long previousCount = previousTransactions.size();

        double incomeTrend = calculateTrend(currentIncome, previousIncome);
        double expensesTrend = calculateTrend(currentExpenses, previousExpenses);
        double balanceTrend = calculateTrend(currentBalance, previousBalance);
        double countTrend = calculateCountTrend(currentCount, previousCount);

        return new TransactionMetricsResponseDTO(
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

    private LocalDateTime[] getDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toLowerCase()) {
            case "week" -> new LocalDateTime[]{
                    now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0),
                    now.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59)
            };
            case "month" -> new LocalDateTime[]{
                    now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                    now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59)
            };
            case "year" -> new LocalDateTime[]{
                    now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0),
                    now.with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59)
            };
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    private LocalDateTime[] getPreviousPeriod(String period) {
        LocalDateTime[] currentRange = getDateRange(period);
        LocalDateTime previousStart;
        LocalDateTime previousEnd;

        switch (period.toLowerCase()) {
            case "week":
                previousStart = currentRange[0].minusWeeks(1);
                previousEnd = currentRange[1].minusWeeks(1);
                break;
            case "month":
                previousStart = currentRange[0].minusMonths(1);
                previousEnd = currentRange[0].minusDays(1).withHour(23).withMinute(59).withSecond(59);
                break;
            case "year":
                previousStart = currentRange[0].minusYears(1);
                previousEnd = currentRange[0].minusDays(1).withHour(23).withMinute(59).withSecond(59);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        return new LocalDateTime[]{previousStart, previousEnd};
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous.abs(), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double calculateCountTrend(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return Math.round(((double)(current - previous) / previous) * 1000.0) / 10.0;
    }
}