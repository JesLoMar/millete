package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.dataexport.domain.model.CategorySnapshot;
import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.PeriodType;
import com.puntomartinez.millete.dataexport.domain.model.PdfExportData;
import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.SavingsGoalSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.*;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Instant;
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
    private final InvestmentUseCases investmentUseCases;
    private final SavingsGoalExportPort savingsGoalExportPort;
    private final UserPreferencesExportPort userPreferencesExportPort;
    private final FileZipExportPort fileZipExportPort;
    private final FileCsvExportPort fileCsvExportPort;
    private final FilePdfExportPort filePdfExportPort;
    private final TimeProvider timeProvider;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    public DataExportService(
            CategoryExportPort categoryExportPort,
            TransactionExportPort transactionExportPort,
            PlannedTransactionExportPort plannedTransactionExportPort,
            InvestmentExportPort investmentExportPort,
            InvestmentUseCases investmentUseCases,
            SavingsGoalExportPort savingsGoalExportPort,
            UserPreferencesExportPort userPreferencesExportPort,
            FileZipExportPort fileZipExportPort,
            FileCsvExportPort fileCsvExportPort,
            @Qualifier("pdfFileExportAdapter")
            FilePdfExportPort filePdfExportPort,
            TimeProvider timeProvider) {

        this.categoryExportPort = categoryExportPort;
        this.transactionExportPort = transactionExportPort;
        this.plannedTransactionExportPort = plannedTransactionExportPort;
        this.investmentExportPort = investmentExportPort;
        this.investmentUseCases = investmentUseCases;
        this.savingsGoalExportPort = savingsGoalExportPort;
        this.userPreferencesExportPort = userPreferencesExportPort;
        this.fileZipExportPort = fileZipExportPort;
        this.fileCsvExportPort = fileCsvExportPort;
        this.filePdfExportPort = filePdfExportPort;
        this.timeProvider = timeProvider;
    }

    public UserDataSnapshot exportAllUserData(UUID userId) {

        log.info("Exportando datos para usuario: {}", userId);

        List<CategorySnapshot> categories =
                categoryExportPort.findByUserId(userId);

        List<TransactionSnapshot> transactions =
                transactionExportPort.findAllByUserId(userId);

        List<PlannedTransactionSnapshot> plannedTransactions =
                plannedTransactionExportPort.findAllByUserId(userId);

        InvestmentLedgerSnapshot investments =
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
                                timeProvider.now(),
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
                                        transaction.description(),
                                        transaction.currency()
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

        List<ExportData.InvestmentExportRow> investments = investmentRows(
                snapshot.investments(), investmentUseCases.portfolio(userId, timeProvider.now()), userId);

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

        List<TransactionSnapshot> periodTransactions =
                transactionExportPort
                        .findByUserIdAndDateBetween(
                                userId,
                                startDate,
                                endDate
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
        BigDecimal totalTransferIn = BigDecimal.ZERO;
        BigDecimal totalTransferOut = BigDecimal.ZERO;
        int transferInCount = 0;
        int transferOutCount = 0;

        Map<String, BigDecimal> expensesByCategory =
                new HashMap<>();

        for (TransactionSnapshot transaction : periodTransactions) {

            if (transaction.type().equals("INCOME")) {
                totalIncome =
                        totalIncome.add(
                                transaction.amount().abs()
                        );
            } else if ("EXPENSE".equals(transaction.type())) {
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
            } else if ("TRANSFER_IN".equals(transaction.type())) {
                totalTransferIn = totalTransferIn.add(transaction.amount().abs());
                transferInCount++;
            } else if ("TRANSFER_OUT".equals(transaction.type())) {
                totalTransferOut = totalTransferOut.add(transaction.amount().abs());
                transferOutCount++;
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

        Instant reportAt = timeProvider.now();
        InvestmentLedgerSnapshot ledger = investmentExportPort.findAllByUserId(userId);
        InvestmentUseCases.PortfolioView portfolio = investmentUseCases.portfolio(userId, reportAt);
        List<ExportData.InvestmentExportRow> investmentRows = investmentRows(ledger, portfolio, userId);
        List<ExportData.InvestmentExportRow> activeInvestments = investmentRows.stream()
                .filter(row -> "POSITION".equals(row.recordType()) || "REALIZED_RESULT".equals(row.recordType())).toList();

        BigDecimal investmentsTotalValue = investmentRows.stream()
                .filter(row -> "POSITION".equals(row.recordType()))
                .map(ExportData.InvestmentExportRow::value).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PdfExportData.TransactionRow> txRows =
                periodTransactions.stream()
                        .filter(transaction -> "INCOME".equals(transaction.type())
                                || "EXPENSE".equals(transaction.type()))
                        .map(transaction ->
                                new PdfExportData.TransactionRow(
                                        transaction.date(),
                                        categoryNames.getOrDefault(
                                                transaction.categoryId(),
                                                "Sin categoría"
                                        ),
                                        transaction.description(),
                                        "INCOME".equals(transaction.type())
                                                ? "Ingreso" : "Gasto",
                                        transaction.amount().abs(),
                                        transaction.currency()
                                )
                        )
                        .toList();

        List<PdfExportData.TransferRow> transferRows = periodTransactions.stream()
                .filter(transaction -> "TRANSFER_IN".equals(transaction.type())
                        || "TRANSFER_OUT".equals(transaction.type()))
                .map(transaction -> new PdfExportData.TransferRow(
                        transaction.date(),
                        transaction.description(),
                        transaction.investmentActivityId() != null
                                ? ("TRANSFER_IN".equals(transaction.type())
                                    ? "Transferencia de inversión recibida" : "Transferencia de inversión enviada")
                                : ("TRANSFER_IN".equals(transaction.type())
                                    ? "Transferencia recibida" : "Transferencia enviada"),
                        transaction.amount().abs(),
                        transaction.currency(),
                        transaction.investmentActivityId() != null
                ))
                .toList();

        List<PdfExportData.InvestmentRow> invRows = activeInvestments.stream().map(row -> {
            String type = row.type() == null ? "" : row.type();
            BigDecimal basis = row.acquisitionCost() == null ? BigDecimal.ZERO : row.acquisitionCost();
            double pct = basis.signum() == 0 || row.result() == null ? 0.0
                    : row.result().multiply(new BigDecimal("100")).divide(basis, 4, RoundingMode.HALF_UP).doubleValue();
            return new PdfExportData.InvestmentRow(row.assetName(), row.ticker(), type, row.quantity(), basis,
                    row.value(), row.value(), row.result(), pct, row.costCurrency(), row.valueCurrency(),
                    row.resultCurrency(), row.recordType());
        }).toList();

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
                        txRows.size(),
                        totalTransferIn,
                        totalTransferOut,
                        transferInCount,
                        transferOutCount,
                        topCategoryName,
                        topCategoryAmount,
                        topCategoryPercentage,
                        investmentsTotalValue,
                        (int) activeInvestments.stream().filter(row -> "POSITION".equals(row.recordType())).count(),
                        sgRows.size(),
                        totalSavedAmount,
                        portfolio.localCurrency()
                );

        return new PdfExportData(
                period.getDisplayName(),
                startDate,
                endDate,
                summary,
                txRows,
                transferRows,
                invRows,
                sgRows,
                portfolio.cashBalances().entrySet().stream()
                        .map(e -> new PdfExportData.CashRow(e.getKey(), e.getValue())).toList()
        );
    }

    private List<ExportData.InvestmentExportRow> investmentRows(InvestmentLedgerSnapshot ledger,
            InvestmentUseCases.PortfolioView portfolio, UUID userId) {
        Map<UUID, InvestmentLedgerSnapshot.AssetSnapshot> assets = safe(ledger.assets()).stream()
                .collect(java.util.stream.Collectors.toMap(InvestmentLedgerSnapshot.AssetSnapshot::id, a -> a));
        List<ExportData.InvestmentExportRow> rows = new java.util.ArrayList<>();
        for (InvestmentUseCases.PositionView position : portfolio.positions()) {
            InvestmentLedgerSnapshot.AssetSnapshot asset = assets.get(position.assetId());
            rows.add(new ExportData.InvestmentExportRow("POSITION", position.assetName(), position.symbol(),
                    position.quantity(), position.costBasis(), position.assetCurrency(), position.marketValue(),
                    position.valuationCurrency(), position.unrealizedGain(), position.valuationCurrency(),
                    asset == null || asset.type() == null ? "" : asset.type().name()));
        }
        portfolio.cashBalances().forEach((currency, balance) -> rows.add(new ExportData.InvestmentExportRow(
                "CASH", "", "", null, null, currency, balance, currency, null, currency, "")));
        for (InvestmentUseCases.ClosedPositionView closed : investmentUseCases.closedPositions(userId, null, portfolio.asOf())) {
            rows.add(new ExportData.InvestmentExportRow("REALIZED_RESULT", closed.assetName(), closed.symbol(),
                    closed.quantity(), closed.costBasis(), closed.currency(), closed.proceeds(), closed.currency(),
                    closed.realizedGain(), closed.currency(), "Realized"));
        }
        return List.copyOf(rows);
    }

    private static <T> List<T> safe(List<T> values) { return values == null ? List.of() : values; }

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
