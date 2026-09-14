package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.dataexport.domain.model.CategorySnapshot;
import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.InvestmentSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.PeriodType;
import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.SavingsGoalSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class DataExportService {

    private final CategoryExportPort categoryExportPort;
    private final TransactionExportPort transactionExportPort;
    private final PlannedTransactionExportPort plannedTransactionExportPort;
    private final InvestmentExportPort investmentExportPort;
    private final SavingsGoalExportPort savingsGoalExportPort;
    private final UserPreferencesExportPort userPreferencesExportPort;
    private final FileZipExportPort fileZipExportPort;
    private final FileCsvExportPort fileCsvExportPort;
    private final FilePdfExportPort filePdfExportPort;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    public DataExportService(
            CategoryExportPort categoryExportPort,
            TransactionExportPort transactionExportPort,
            PlannedTransactionExportPort plannedTransactionExportPort,
            InvestmentExportPort investmentExportPort,
            SavingsGoalExportPort savingsGoalExportPort,
            UserPreferencesExportPort userPreferencesExportPort,
            FileZipExportPort fileZipExportPort,
            FileCsvExportPort fileCsvExportPort,
            @Qualifier("pdfFileExportAdapter")
            FilePdfExportPort filePdfExportPort) {

        this.categoryExportPort = categoryExportPort;
        this.transactionExportPort = transactionExportPort;
        this.plannedTransactionExportPort = plannedTransactionExportPort;
        this.investmentExportPort = investmentExportPort;
        this.savingsGoalExportPort = savingsGoalExportPort;
        this.userPreferencesExportPort = userPreferencesExportPort;
        this.fileZipExportPort = fileZipExportPort;
        this.fileCsvExportPort = fileCsvExportPort;
        this.filePdfExportPort = filePdfExportPort;
    }

    public UserDataSnapshot exportAllUserData(UUID userId) {

        log.info("Exportando datos para usuario: {}", userId);

        List<CategorySnapshot> categories =
                categoryExportPort.findByUserId(userId);

        List<TransactionSnapshot> transactions =
                transactionExportPort.findAllByUserId(userId);

        List<PlannedTransactionSnapshot> plannedTransactions =
                plannedTransactionExportPort.findAllByUserId(userId);

        List<InvestmentSnapshot> investments =
                investmentExportPort.findAllByUserId(userId);

        List<SavingsGoalSnapshot> savingsGoals =
                savingsGoalExportPort.findAllByUserId(userId);

        UserPreferencesSnapshot userPreferences =
                userPreferencesExportPort
                        .findByUserId(userId)
                        .orElse(null);

        UserDataSnapshot snapshot =
                new UserDataSnapshot(
                        new UserDataSnapshot.SnapshotMetadata(
                                ExportVersion.CURRENT.toString(),
                                LocalDateTime.now(),
                                appVersion
                        ),
                        categories,
                        transactions,
                        plannedTransactions,
                        investments,
                        savingsGoals,
                        userPreferences
                );

        log.info(
                "Exportación completada. v{}",
                ExportVersion.CURRENT
        );

        return snapshot;
    }

    public ExportData buildExportData(UUID userId) {

        UserDataSnapshot snapshot =
                exportAllUserData(userId);

        Map<UUID, String> categoryNames = new HashMap<>();

        for (CategorySnapshot category : snapshot.categories()) {
            categoryNames.put(
                    category.id(),
                    category.name()
            );
        }

        List<ExportData.CategoryExportRow> categories =
                snapshot.categories().stream()
                        .filter(CategorySnapshot::active)
                        .map(category ->
                                new ExportData.CategoryExportRow(
                                        category.name(),
                                        category.budgetLimit()
                                )
                        )
                        .toList();

        List<ExportData.TransactionExportRow> transactions =
                snapshot.transactions().stream()
                        .filter(TransactionSnapshot::active)
                        .map(transaction ->
                                new ExportData.TransactionExportRow(
                                        categoryNames.getOrDefault(
                                                transaction.categoryId(),
                                                "Sin categoría"
                                        ),
                                        transaction.amount(),
                                        transaction.date(),
                                        transaction.type(),
                                        transaction.description()
                                )
                        )
                        .toList();

        List<ExportData.PlannedTransactionExportRow> planned =
                snapshot.plannedTransactions().stream()
                        .filter(PlannedTransactionSnapshot::active)
                        .map(plannedTransaction ->
                                new ExportData.PlannedTransactionExportRow(
                                        categoryNames.getOrDefault(
                                                plannedTransaction.categoryId(),
                                                "Sin categoría"
                                        ),
                                        plannedTransaction.amount(),
                                        plannedTransaction.type(),
                                        plannedTransaction.description(),
                                        plannedTransaction.frequencyType(),
                                        plannedTransaction.frequencyInterval(),
                                        plannedTransaction.startDate(),
                                        plannedTransaction.endDate(),
                                        plannedTransaction.lastExecutedDate()
                                )
                        )
                        .toList();

        List<ExportData.InvestmentExportRow> investments =
                snapshot.investments().stream()
                        .filter(InvestmentSnapshot::active)
                        .map(investment ->
                                new ExportData.InvestmentExportRow(
                                        investment.assetName(),
                                        investment.ticker(),
                                        investment.quantity(),
                                        investment.purchasePrice(),
                                        investment.currentPrice(),
                                        investment.type(),
                                        investment.purchaseDate().atStartOfDay()
                                )
                        )
                        .toList();

        List<ExportData.SavingsGoalExportRow> savingsGoals =
                snapshot.savingsGoals().stream()
                        .filter(SavingsGoalSnapshot::active)
                        .map(goal ->
                                new ExportData.SavingsGoalExportRow(
                                        goal.name(),
                                        goal.targetAmount(),
                                        goal.currentAmount(),
                                        calculatePercentage(
                                                goal.currentAmount(),
                                                goal.targetAmount()
                                        ),
                                        goal.deadline(),
                                        goal.priority(),
                                        goal.link()
                                )
                        )
                        .toList();

        return new ExportData(
                categories,
                transactions,
                planned,
                investments,
                savingsGoals
        );
    }

    public PdfExportData buildPdfExportData(
            UUID userId,
            PeriodType period) {

        LocalDate endDate = period.getEndDate();
        LocalDate startDate = period.getStartDate();

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate.atTime(LocalTime.MAX);

        List<TransactionSnapshot> periodTransactions =
                transactionExportPort
                        .findByUserIdAndDateBetween(
                                userId,
                                startDateTime,
                                endDateTime
                        )
                        .stream()
                        .filter(TransactionSnapshot::active)
                        .sorted(
                                Comparator.comparing(
                                        TransactionSnapshot::date
                                ).reversed()
                        )
                        .toList();

        List<CategorySnapshot> categories =
                categoryExportPort.findByUserId(userId);

        Map<UUID, String> categoryNames = new HashMap<>();

        for (CategorySnapshot category : categories) {
            categoryNames.put(
                    category.id(),
                    category.name()
            );
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        Map<String, BigDecimal> expensesByCategory =
                new HashMap<>();

        for (TransactionSnapshot transaction : periodTransactions) {

            if (transaction.type().equals("INCOME")) {
                totalIncome =
                        totalIncome.add(
                                transaction.amount().abs()
                        );
            } else {
                totalExpenses =
                        totalExpenses.add(
                                transaction.amount().abs()
                        );

                String categoryName =
                        categoryNames.getOrDefault(
                                transaction.categoryId(),
                                "Sin categoría"
                        );

                expensesByCategory.merge(
                        categoryName,
                        transaction.amount().abs(),
                        BigDecimal::add
                );
            }
        }

        BigDecimal balance =
                totalIncome.subtract(totalExpenses);

        String topCategoryName = "—";
        BigDecimal topCategoryAmount = BigDecimal.ZERO;
        double topCategoryPercentage = 0.0;

        if (!expensesByCategory.isEmpty()) {

            var topEntry =
                    expensesByCategory.entrySet()
                            .stream()
                            .max(Map.Entry.comparingByValue())
                            .orElse(null);

            if (topEntry != null) {
                topCategoryName = topEntry.getKey();
                topCategoryAmount = topEntry.getValue();

                topCategoryPercentage =
                        calculatePercentage(
                                topCategoryAmount,
                                totalExpenses
                        );
            }
        }

        List<InvestmentSnapshot> activeInvestments =
                investmentExportPort
                        .findAllByUserId(userId)
                        .stream()
                        .filter(InvestmentSnapshot::active)
                        .toList();

        BigDecimal investmentsTotalValue =
                activeInvestments.stream()
                        .map(InvestmentSnapshot::currentValue)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<PdfExportData.TransactionRow> txRows =
                periodTransactions.stream()
                        .map(transaction ->
                                new PdfExportData.TransactionRow(
                                        transaction.date(),
                                        categoryNames.getOrDefault(
                                                transaction.categoryId(),
                                                "Sin categoría"
                                        ),
                                        transaction.description(),
                                        transaction.type()
                                                .equals("INCOME")
                                                ? "Ingreso"
                                                : "Gasto",
                                        transaction.amount().abs()
                                )
                        )
                        .toList();

        List<PdfExportData.InvestmentRow> invRows =
                activeInvestments.stream()
                        .map(investment ->
                                new PdfExportData.InvestmentRow(
                                        investment.assetName(),
                                        investment.ticker(),
                                        investment.type() != null
                                                ? investment.type()
                                                : "",
                                        investment.quantity(),
                                        investment.purchasePrice(),
                                        investment.currentPrice(),
                                        investment.currentValue(),
                                        investment.profitOrLoss(),
                                        investment.returnOnInvestmentPercentage()
                                                .doubleValue()
                                )
                        )
                        .toList();

        List<SavingsGoalSnapshot> activeSavingsGoals =
                savingsGoalExportPort
                        .findAllByUserId(userId)
                        .stream()
                        .filter(SavingsGoalSnapshot::active)
                        .toList();

        List<PdfExportData.SavingsGoalRow> sgRows =
                activeSavingsGoals.stream()
                        .map(goal ->
                                new PdfExportData.SavingsGoalRow(
                                        goal.name(),
                                        goal.targetAmount(),
                                        goal.currentAmount(),
                                        calculatePercentage(
                                                goal.currentAmount(),
                                                goal.targetAmount()
                                        ),
                                        goal.deadline(),
                                        goal.priority(),
                                        goal.link()
                                )
                        )
                        .toList();

        BigDecimal totalSavedAmount =
                sgRows.stream()
                        .map(
                                PdfExportData.SavingsGoalRow::currentAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        PdfExportData.Summary summary =
                new PdfExportData.Summary(
                        balance,
                        totalIncome,
                        totalExpenses,
                        periodTransactions.size(),
                        topCategoryName,
                        topCategoryAmount,
                        topCategoryPercentage,
                        investmentsTotalValue,
                        activeInvestments.size(),
                        sgRows.size(),
                        totalSavedAmount
                );

        return new PdfExportData(
                period.getDisplayName(),
                startDate,
                endDate,
                summary,
                txRows,
                invRows,
                sgRows
        );
    }

    private double calculatePercentage(
            BigDecimal currentAmount,
            BigDecimal targetAmount) {

        if (currentAmount == null
                || targetAmount == null
                || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {

            return 0.0;
        }

        return currentAmount
                .multiply(new BigDecimal("100"))
                .divide(
                        targetAmount,
                        1,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    public byte[] exportUserDataAsZip(UUID userId) {

        log.info(
                "Exportando datos ZIP para usuario: {}",
                userId
        );

        ExportData data =
                buildExportData(userId);

        byte[] zip =
                fileZipExportPort.generateZip(data);

        log.info(
                "Exportación ZIP completada para usuario: {}",
                userId
        );

        return zip;
    }

    public byte[] exportUserDataAsCsv(
            UUID userId,
            String entityType) {

        log.info(
                "Exportando datos CSV ({}) para usuario: {}",
                entityType,
                userId
        );

        ExportData data =
                buildExportData(userId);

        byte[] csv =
                fileCsvExportPort.generateCsv(
                        data,
                        entityType
                );

        log.info(
                "Exportación CSV ({}) completada para usuario: {}",
                entityType,
                userId
        );

        return csv;
    }

    public byte[] exportUserDataAsPdf(
            UUID userId,
            PeriodType period) {

        log.info(
                "Exportando datos PDF para usuario: {} (periodo: {})",
                userId,
                period.getCode()
        );

        PdfExportData data =
                buildPdfExportData(
                        userId,
                        period
                );

        byte[] pdf =
                filePdfExportPort.generatePdf(data);

        log.info(
                "Exportación PDF completada para usuario: {}",
                userId
        );

        return pdf;
    }
}