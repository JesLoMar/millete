package com.puntomartinez.millete.dataexport.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PdfExportData(
        String periodDisplayName,
        LocalDate startDate,
        LocalDate endDate,
        Summary summary,
        List<TransactionRow> transactions,
        List<TransferRow> transfers,
        List<InvestmentRow> investments,
        List<SavingsGoalRow> savingsGoals
) {
    public PdfExportData(String periodDisplayName, LocalDate startDate, LocalDate endDate,
            Summary summary, List<TransactionRow> transactions, List<InvestmentRow> investments,
            List<SavingsGoalRow> savingsGoals) {
        this(periodDisplayName, startDate, endDate, summary, transactions, List.of(), investments, savingsGoals);
    }

    public record Summary(
            BigDecimal balance,
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            int transactionCount,
            BigDecimal transferIn,
            BigDecimal transferOut,
            int transferInCount,
            int transferOutCount,
            String topCategoryName,
            BigDecimal topCategoryAmount,
            double topCategoryPercentage,
            BigDecimal investmentsTotalValue,
            int activeInvestmentsCount,
            int activeSavingsGoalsCount,
            BigDecimal totalSavedAmount
    ) {
        public Summary(BigDecimal balance, BigDecimal totalIncome, BigDecimal totalExpenses,
                int transactionCount, String topCategoryName, BigDecimal topCategoryAmount,
                double topCategoryPercentage, BigDecimal investmentsTotalValue,
                int activeInvestmentsCount, int activeSavingsGoalsCount, BigDecimal totalSavedAmount) {
            this(balance, totalIncome, totalExpenses, transactionCount, BigDecimal.ZERO,
                    BigDecimal.ZERO, 0, 0, topCategoryName, topCategoryAmount,
                    topCategoryPercentage, investmentsTotalValue, activeInvestmentsCount,
                    activeSavingsGoalsCount, totalSavedAmount);
        }
    }

    public record TransferRow(
            LocalDate date,
            String description,
            String type,
            BigDecimal amount
    ) {}

    public record TransactionRow(
            LocalDate date,
            String categoryName,
            String description,
            String type,
            BigDecimal amount
    ) {}

    public record InvestmentRow(
            String assetName,
            String ticker,
            String type,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            BigDecimal currentPrice,
            BigDecimal currentValue,
            BigDecimal profitLoss,
            double returnPercentage
    ) {}

    public record SavingsGoalRow(
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            double progress,
            LocalDate deadline,
            String priority,
            String link
    ) {}
}
