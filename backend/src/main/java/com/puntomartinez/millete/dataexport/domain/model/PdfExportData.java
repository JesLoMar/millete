package com.puntomartinez.millete.dataexport.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            int activeSavingsGoalsCount,
            BigDecimal totalSavedAmount
    ) {}

    public record TransferRow(
            LocalDate date,
            String description,
            String type,
            BigDecimal amount,
            String currency,
            boolean investmentGenerated
    ) {}

    public record TransactionRow(
            LocalDate date,
            String categoryName,
            String description,
            String type,
            BigDecimal amount,
            String currency
    ) {}

    public record InvestmentRow(
            String assetName,
            BigDecimal latestPrice,
            String latestPriceCurrency,
            BigDecimal averagePurchasePrice,
            String purchasePriceCurrency
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
