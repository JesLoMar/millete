package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.FileExportPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.puntomartinez.millete.dataexport.domain.model.ExportData;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.investments.domain.model.Investment;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class DataExportService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PlannedTransactionRepository plannedTransactionRepository;
    private final InvestmentRepository investmentRepository;
    private final FileExportPort fileExportPort;


    @Value("${app.version:0.0.1}")
    private String appVersion;

    public DataExportService(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            PlannedTransactionRepository plannedTransactionRepository,
            InvestmentRepository investmentRepository,
            @Qualifier("zipFileExportAdapter") FileExportPort fileExportPort) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.investmentRepository = investmentRepository;
        this.fileExportPort = fileExportPort;
    }

    public UserDataSnapshot exportAllUserData(UUID userId) {
        log.info("Exportando datos para usuario: {}", userId);

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        ExportVersion.CURRENT.toString(),
                        LocalDateTime.now(),
                        appVersion
                ),
                categoryRepository.findByIdUsuario(userId),
                transactionRepository.findAllByUserId(userId),
                plannedTransactionRepository.findAllByUserId(userId),
                investmentRepository.findAllByUserId(userId)
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

        return new ExportData(categories, transactions, planned, investments);
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
}