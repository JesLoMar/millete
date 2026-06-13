package com.puntomartinez.millete.dataexport.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.migration.MigrationChain;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class DataImportService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PlannedTransactionRepository plannedTransactionRepository;
    private final InvestmentRepository investmentRepository;
    private final MigrationChain migrationChain;
    private final ObjectMapper objectMapper;

    public DataImportService(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            PlannedTransactionRepository plannedTransactionRepository,
            InvestmentRepository investmentRepository,
            MigrationChain migrationChain) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.investmentRepository = investmentRepository;
        this.migrationChain = migrationChain;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public String importUserData(MultipartFile file, UUID loggedInUserId) {
        try (InputStream inputStream = file.getInputStream()) {

            log.debug("Leyendo archivo de importación...");
            UserDataSnapshot snapshot = objectMapper.readValue(inputStream, UserDataSnapshot.class);
            log.info("Archivo leído. v{}", snapshot.metadata().version());

            snapshot = validateAndMigrate(snapshot);

            int totalImported = importCategories(snapshot, loggedInUserId);
            totalImported += importTransactions(snapshot, loggedInUserId);
            totalImported += importPlannedTransactions(snapshot, loggedInUserId);
            totalImported += importInvestments(snapshot, loggedInUserId);

            String summary = String.format(
                    "Importación exitosa. %d registros importados. v%s",
                    totalImported, ExportVersion.CURRENT);

            log.info(summary);
            return summary;

        } catch (Exception e) {
            log.error("Error al importar: {}", e.getMessage(), e);
            throw new RuntimeException(
                    "Error al importar: " + e.getMessage()
                            + ". Asegúrate de que el archivo sea compatible con v"
                            + ExportVersion.CURRENT, e);
        }
    }

    // ─── Validación de versión y migración ────────────────

    private UserDataSnapshot validateAndMigrate(UserDataSnapshot snapshot) {
        ExportVersion fileVersion = ExportVersion.fromString(snapshot.metadata().version());

        if (!fileVersion.isCompatibleWith(ExportVersion.CURRENT)) {
            throw new RuntimeException(
                    String.format("Versión incompatible. Archivo v%s, sistema v%s.",
                            fileVersion, ExportVersion.CURRENT));
        }

        if (fileVersion.needsMigration(ExportVersion.CURRENT)) {
            log.warn("Migrando de v{} a v{}", fileVersion, ExportVersion.CURRENT);
            return migrationChain.migrateToLatest(snapshot);
        }

        log.debug("v{} compatible", fileVersion);
        return snapshot;
    }

    // ─── Importación por entidad ────────────────────────

    private int importCategories(UserDataSnapshot snapshot, UUID loggedInUserId) {
        if (snapshot.categories() == null || snapshot.categories().isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Category cat : snapshot.categories()) {
            Category safeCat = new Category(
                    cat.getId(), loggedInUserId, cat.getName(), cat.getColor(),
                    cat.getBudgetLimit(), cat.getCreatedAt(), cat.getModifiedAt(), cat.isActive()
            );
            categoryRepository.save(safeCat);
            count++;
        }
        log.debug("Categorías: {}", count);
        return count;
    }

    private int importTransactions(UserDataSnapshot snapshot, UUID loggedInUserId) {
        if (snapshot.transactions() == null || snapshot.transactions().isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Transaction tx : snapshot.transactions()) {
            Transaction safeTx = new Transaction(
                    tx.getId(), loggedInUserId, tx.getCategoryId(), tx.getAmount(),
                    tx.getDate(), tx.getType(), tx.getDescription(),
                    tx.getCreatedAt(), tx.getModifiedAt(), tx.isActive()
            );
            transactionRepository.save(safeTx);
            count++;
        }
        log.debug("Transacciones: {}", count);
        return count;
    }

    private int importPlannedTransactions(UserDataSnapshot snapshot, UUID loggedInUserId) {
        if (snapshot.plannedTransactions() == null || snapshot.plannedTransactions().isEmpty()) {
            return 0;
        }

        int count = 0;
        for (PlannedTransaction ptx : snapshot.plannedTransactions()) {
            PlannedTransaction safePtx = new PlannedTransaction(
                    ptx.getId(), loggedInUserId, ptx.getCategoryId(), ptx.getAmount(),
                    ptx.getType(), ptx.getDescription(), ptx.getFrequencyType(),
                    ptx.getFrequencyInterval(), ptx.getStartDate(), ptx.getEndDate(),
                    ptx.getCreatedAt(), ptx.getModifiedAt(), ptx.isActive()
            );
            plannedTransactionRepository.save(safePtx);
            count++;
        }
        log.debug("Transacciones programadas: {}", count);
        return count;
    }

    private int importInvestments(UserDataSnapshot snapshot, UUID loggedInUserId) {
        if (snapshot.investments() == null || snapshot.investments().isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Investment inv : snapshot.investments()) {
            Investment safeInv = new Investment(
                    inv.getId(), loggedInUserId, inv.getAssetName(), inv.getTicker(),
                    inv.getQuantity(), inv.getPurchasePrice(), inv.getCurrentPrice(),
                    inv.getType(), inv.getPurchaseDate(),
                    inv.getCreatedAt(), inv.getModifiedAt(), inv.isActive()
            );
            investmentRepository.save(safeInv);
            count++;
        }
        log.debug("Inversiones: {}", count);
        return count;
    }
}