package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.model.*;
import com.puntomartinez.millete.dataexport.domain.ports.out.FileExportPort;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class DataExportService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PlannedTransactionRepository plannedTransactionRepository;
    private final InvestmentRepository investmentRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final GoalUnitRepository goalUnitRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final FileExportPort fileExportPort;
    private final FileExportPort pdfFileExportPort;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    public DataExportService(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            PlannedTransactionRepository plannedTransactionRepository,
            InvestmentRepository investmentRepository,
            SavingsGoalRepository savingsGoalRepository,
            UserPreferencesRepository userPreferencesRepository,
            GoalUnitRepository goalUnitRepository,
            GoalMemberRepository goalMemberRepository,
            GoalContributionRepository goalContributionRepository,
            @Qualifier("zipFileExportAdapter") FileExportPort fileExportPort,
            @Qualifier("pdfFileExportAdapter") FileExportPort pdfFileExportPort) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.investmentRepository = investmentRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.goalUnitRepository = goalUnitRepository;
        this.goalMemberRepository = goalMemberRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.fileExportPort = fileExportPort;
        this.pdfFileExportPort = pdfFileExportPort;
    }

    public UserDataSnapshot exportAllUserData(UUID userId) {
        log.info("Exportando datos para usuario: {}", userId);


        List<GoalMember> userGoalMembers = goalMemberRepository.findByUserId(userId);
        Set<UUID> userGoalIds = new HashSet<>();
        for (GoalMember gm : userGoalMembers) {
            userGoalIds.add(gm.getGoalId());
        }

        List<GoalUnit> goalUnits = new ArrayList<>();
        List<GoalMember> goalMembers = new ArrayList<>();
        List<GoalContribution> goalContributions = new ArrayList<>();

        for (UUID goalId : userGoalIds) {
            goalUnitRepository.findById(goalId).ifPresent(goalUnits::add);
            goalMembers.addAll(goalMemberRepository.findByGoalId(goalId));
            goalContributions.addAll(goalContributionRepository.findByGoalId(goalId));
        }

        UserPreferences userPreferences = userPreferencesRepository.findByUserId(userId).orElse(null);

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        ExportVersion.CURRENT.toString(),
                        LocalDateTime.now(),
                        appVersion
                ),
                categoryRepository.findByIdUsuario(userId),
                transactionRepository.findAllByUserId(userId),
                plannedTransactionRepository.findAllByUserId(userId),
                investmentRepository.findAllByUserId(userId),
                savingsGoalRepository.findAllByUserId(userId),
                userPreferences,
                goalUnits,
                goalMembers,
                goalContributions
        );

        log.info("Exportación completada. v{}", ExportVersion.CURRENT);
        return snapshot;
    }

    public ExportData buildExportData(UUID userId) {
        UserDataSnapshot snapshot = exportAllUserData(userId);

        Map<UUID, String> categoryNames = new HashMap<>();
        if (snapshot.categories() != null) {
            for (Category cat : snapshot.categories()) {
                categoryNames.put(cat.getId(), cat.getName());
            }
        }

        List<ExportData.CategoryExportRow> categories = snapshot.categories() != null
                ? snapshot.categories().stream()
                .filter(Category::isActive)
                .map(c -> new ExportData.CategoryExportRow(c.getName(), c.getBudgetLimit()))
                .toList()
                : List.of();

        List<ExportData.TransactionExportRow> transactions = snapshot.transactions() != null
                ? snapshot.transactions().stream()
                .filter(Transaction::isActive)
                .map(tx -> new ExportData.TransactionExportRow(
                        categoryNames.getOrDefault(tx.getCategoryId(), "Sin categoría"),
                        tx.getAmount(), tx.getDate(), tx.getType().name(), tx.getDescription()))
                .toList()
                : List.of();

        List<ExportData.PlannedTransactionExportRow> planned = snapshot.plannedTransactions() != null
                ? snapshot.plannedTransactions().stream()
                .filter(PlannedTransaction::isActive)
                .map(ptx -> new ExportData.PlannedTransactionExportRow(
                        categoryNames.getOrDefault(ptx.getCategoryId(), "Sin categoría"),
                        ptx.getAmount(), ptx.getType().name(), ptx.getDescription(),
                        ptx.getFrequencyType().name(), ptx.getFrequencyInterval(),
                        ptx.getStartDate(), ptx.getEndDate(), ptx.getLastExecutedDate()))
                .toList()
                : List.of();

        List<ExportData.InvestmentExportRow> investments = snapshot.investments() != null
                ? snapshot.investments().stream()
                .filter(Investment::isActive)
                .map(inv -> new ExportData.InvestmentExportRow(
                        inv.getAssetName(), inv.getTicker(), inv.getQuantity(),
                        inv.getPurchasePrice(), inv.getCurrentPrice(),
                        inv.getType() != null ? inv.getType().name() : null,
                        inv.getPurchaseDate()))
                .toList()
                : List.of();

        List<ExportData.SavingsGoalExportRow> savingsGoals = snapshot.savingsGoals() != null
                ? snapshot.savingsGoals().stream()
                .filter(SavingsGoal::isActive)
                .map(sg -> new ExportData.SavingsGoalExportRow(
                        sg.getName(),
                        sg.getTargetAmount(),
                        sg.getCurrentAmount(),
                        sg.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                        ? sg.getCurrentAmount().multiply(new BigDecimal("100"))
                                .divide(sg.getTargetAmount(), 1, RoundingMode.HALF_UP).doubleValue()
                        : 0.0,
                        sg.getDeadline(),
                        sg.getPriority(),
                        sg.getStatus(),
                        sg.getLink()))
                .toList()
                : List.of();

        return new ExportData(categories, transactions, planned, investments, savingsGoals);
    }

    public PdfExportData buildPdfExportData(UUID userId, PeriodType period) {
        LocalDate endDate = period.getEndDate();
        LocalDate startDate = period.getStartDate();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        UserDataSnapshot snapshot = exportAllUserData(userId);

        List<Transaction> periodTransactions = snapshot.transactions() != null
                ? snapshot.transactions().stream()
                .filter(Transaction::isActive)
                .filter(tx -> !tx.getDate().isBefore(startDateTime) && !tx.getDate().isAfter(endDateTime))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .toList()
                : List.of();

        Map<UUID, String> categoryNames = new HashMap<>();
        if (snapshot.categories() != null) {
            for (Category cat : snapshot.categories()) {
                categoryNames.put(cat.getId(), cat.getName());
            }
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        for (Transaction tx : periodTransactions) {
            if (tx.getType().name().equals("INCOME")) {
                totalIncome = totalIncome.add(tx.getAmount().abs());
            } else {
                totalExpenses = totalExpenses.add(tx.getAmount().abs());
                String catName = categoryNames.getOrDefault(tx.getCategoryId(), "Sin categoría");
                expensesByCategory.merge(catName, tx.getAmount().abs(), BigDecimal::add);
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        String topCategoryName = "—";
        BigDecimal topCategoryAmount = BigDecimal.ZERO;
        double topCategoryPercentage = 0.0;

        if (!expensesByCategory.isEmpty()) {
            var topEntry = expensesByCategory.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            if (topEntry != null) {
                topCategoryName = topEntry.getKey();
                topCategoryAmount = topEntry.getValue();
                topCategoryPercentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                        ? topCategoryAmount.multiply(new BigDecimal("100"))
                        .divide(totalExpenses, 1, RoundingMode.HALF_UP).doubleValue()
                        : 0.0;
            }
        }

        List<Investment> activeInvestments = snapshot.investments() != null
                ? snapshot.investments().stream()
                .filter(Investment::isActive)
                .toList()
                : List.of();

        BigDecimal investmentsTotalValue = BigDecimal.ZERO;
        for (Investment inv : activeInvestments) {
            investmentsTotalValue = investmentsTotalValue.add(inv.getCurrentValue());
        }

        List<PdfExportData.TransactionRow> txRows = periodTransactions.stream()
                .map(tx -> new PdfExportData.TransactionRow(
                        tx.getDate(),
                        categoryNames.getOrDefault(tx.getCategoryId(), "Sin categoría"),
                        tx.getDescription(),
                        tx.getType().name().equals("INCOME") ? "Ingreso" : "Gasto",
                        tx.getAmount().abs()))
                .toList();

        List<PdfExportData.InvestmentRow> invRows = activeInvestments.stream()
                .map(inv -> new PdfExportData.InvestmentRow(
                        inv.getAssetName(),
                        inv.getTicker(),
                        inv.getType() != null ? inv.getType().name() : "",
                        inv.getQuantity(),
                        inv.getPurchasePrice(),
                        inv.getCurrentPrice(),
                        inv.getCurrentValue(),
                        inv.getProfitOrLoss(),
                        inv.getReturnOnInvestmentPercentage().doubleValue()))
                .toList();

        List<PdfExportData.SavingsGoalRow> sgRows = snapshot.savingsGoals() != null
                ? snapshot.savingsGoals().stream()
                .filter(SavingsGoal::isActive)
                .map(sg -> new PdfExportData.SavingsGoalRow(
                        sg.getName(),
                        sg.getTargetAmount(),
                        sg.getCurrentAmount(),
                        sg.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                        ? sg.getCurrentAmount().multiply(new BigDecimal("100"))
                                .divide(sg.getTargetAmount(), 1, RoundingMode.HALF_UP).doubleValue()
                        : 0.0,
                        sg.getDeadline(),
                        sg.getPriority(),
                        sg.getStatus(),
                        sg.getLink()))
                .toList()
                : List.of();

        int activeSavingsGoalsCount = sgRows.size();

        BigDecimal totalSavedAmount = sgRows.stream()
                .map(PdfExportData.SavingsGoalRow::currentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PdfExportData.Summary summary = new PdfExportData.Summary(
                balance, totalIncome, totalExpenses, periodTransactions.size(),
                topCategoryName, topCategoryAmount, topCategoryPercentage,
                investmentsTotalValue, activeInvestments.size(),
                activeSavingsGoalsCount, totalSavedAmount
        );

        return new PdfExportData(period.getDisplayName(), startDate, endDate, summary, txRows, invRows, sgRows);
    }

    public byte[] exportUserDataAsZip(UUID userId) {
        log.info("Exportando datos ZIP para usuario: {}", userId);
        ExportData data = buildExportData(userId);
        byte[] zip = fileExportPort.generateZip(data);
        log.info("Exportación ZIP completada para usuario: {}", userId);
        return zip;
    }

    public byte[] exportUserDataAsCsv(UUID userId, String entityType) {
        log.info("Exportando datos CSV ({}) para usuario: {}", entityType, userId);
        ExportData data = buildExportData(userId);
        byte[] csv = fileExportPort.generateCsv(data, entityType);
        log.info("Exportación CSV ({}) completada para usuario: {}", entityType, userId);
        return csv;
    }

    public byte[] exportUserDataAsPdf(UUID userId, PeriodType period) {
        log.info("Exportando datos PDF para usuario: {} (periodo: {})", userId, period.getCode());
        PdfExportData data = buildPdfExportData(userId, period);
        byte[] pdf = pdfFileExportPort.generatePdf(data);
        log.info("Exportación PDF completada para usuario: {}", userId);
        return pdf;
    }
}
