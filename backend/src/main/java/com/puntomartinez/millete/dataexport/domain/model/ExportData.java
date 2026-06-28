package com.puntomartinez.millete.dataexport.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExportData(
        List<CategoryExportRow> categories,
        List<TransactionExportRow> transactions,
        List<PlannedTransactionExportRow> plannedTransactions,
        List<InvestmentExportRow> investments,
        List<SavingsGoalExportRow> savingsGoals
) {

    public record CategoryExportRow(
            String name,
            BigDecimal budgetLimit
    ) {}

    public record TransactionExportRow(
            String categoryName,
            BigDecimal amount,
            LocalDateTime date,
            String type,
            String description
    ) {}

    public record PlannedTransactionExportRow(
            String categoryName,
            BigDecimal amount,
            String type,
            String description,
            String frequencyType,
            int frequencyInterval,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate lastExecutedDate
    ) {}

    public record InvestmentExportRow(
            String assetName,
            String ticker,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            BigDecimal currentPrice,
            String type,
            LocalDateTime purchaseDate
    ) {}

    public record SavingsGoalExportRow(
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            double progress,
            LocalDate deadline,
            String priority,
            String status,
            String link
    ) {}
}