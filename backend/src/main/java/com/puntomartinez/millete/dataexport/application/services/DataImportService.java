package com.puntomartinez.millete.dataexport.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.migration.MigrationChain;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
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
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class DataImportService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PlannedTransactionRepository plannedTransactionRepository;
    private final InvestmentRepository investmentRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final GoalUnitRepository goalUnitRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final MigrationChain migrationChain;
    private final ObjectMapper objectMapper;

    public DataImportService(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            PlannedTransactionRepository plannedTransactionRepository,
            InvestmentRepository investmentRepository,
            SavingsGoalRepository savingsGoalRepository,
            UserPreferencesRepository userPreferencesRepository,
            GoalUnitRepository goalUnitRepository,
            GoalMemberRepository goalMemberRepository,
            GoalContributionRepository goalContributionRepository,
            MigrationChain migrationChain) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.investmentRepository = investmentRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.goalUnitRepository = goalUnitRepository;
        this.goalMemberRepository = goalMemberRepository;
        this.goalContributionRepository = goalContributionRepository;
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

            // ─── Sanitización: sobrescribir todos los userId del snapshot ───
            sanitizeSnapshot(snapshot, loggedInUserId);

            Map<UUID, UUID> categoryIdMap = new HashMap<>();
            Map<UUID, UUID> goalIdMap = new HashMap<>();

            int totalImported = importCategories(snapshot, loggedInUserId, categoryIdMap);
            totalImported += importTransactions(snapshot, loggedInUserId, categoryIdMap);
            totalImported += importPlannedTransactions(snapshot, loggedInUserId, categoryIdMap);
            totalImported += importInvestments(snapshot, loggedInUserId);
            totalImported += importSavingsGoals(snapshot, loggedInUserId);
            totalImported += importUserPreferences(snapshot, loggedInUserId);
            totalImported += importGroupGoals(snapshot, loggedInUserId, goalIdMap);

            // ─── Verificación post-importación ───
            verifyImportedTransactions(loggedInUserId, categoryIdMap);

            String summary = String.format(
                    "Importación exitosa. %d registros importados. v%s",
                    totalImported, ExportVersion.CURRENT);

            log.info(summary);
            return summary;

        } catch (Exception e) {
            log.error("Error al importar: {}", e.getMessage(), e);
            throw new InvalidInputException(
                    "Error al importar el archivo. Asegúrate de que sea compatible con v"
                            + ExportVersion.CURRENT, e);
        }
    }

    // ─── Validación de versión y migración ────────────────

    private UserDataSnapshot validateAndMigrate(UserDataSnapshot snapshot) {
        ExportVersion fileVersion = ExportVersion.fromString(snapshot.metadata().version());

        if (!fileVersion.isCompatibleWith(ExportVersion.CURRENT)) {
            throw new InvalidInputException(
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

    // ─── Sanitización: forzar userId del destino ────────────────

    private void sanitizeSnapshot(UserDataSnapshot snapshot, UUID loggedInUserId) {
        if (snapshot.categories() != null) {
            for (Category cat : snapshot.categories()) {
                cat.setUserId(loggedInUserId);
            }
        }
        if (snapshot.transactions() != null) {
            for (Transaction tx : snapshot.transactions()) {
                tx.setUserId(loggedInUserId);
            }
        }
        if (snapshot.plannedTransactions() != null) {
            for (PlannedTransaction ptx : snapshot.plannedTransactions()) {
                ptx.setUserId(loggedInUserId);
            }
        }
        if (snapshot.investments() != null) {
            for (Investment inv : snapshot.investments()) {
                inv.setUserId(loggedInUserId);
            }
        }
        if (snapshot.savingsGoals() != null) {
            for (SavingsGoal sg : snapshot.savingsGoals()) {
                sg.setUserId(loggedInUserId);
            }
        }
        if (snapshot.goalMembers() != null) {
            for (GoalMember gm : snapshot.goalMembers()) {
                gm.setUserId(loggedInUserId);
            }
        }
        if (snapshot.goalContributions() != null) {
            for (GoalContribution gc : snapshot.goalContributions()) {
                gc.setUserId(loggedInUserId);
            }
        }
        if (snapshot.userPreferences() != null) {
            snapshot.userPreferences().setUserId(loggedInUserId);
        }
        log.debug("Snapshot sanitizado con userId destino: {}", loggedInUserId);
    }

    // ─── Importación por entidad ────────────────────────

    private int importCategories(UserDataSnapshot snapshot, UUID loggedInUserId, Map<UUID, UUID> categoryIdMap) {
        if (snapshot.categories() == null || snapshot.categories().isEmpty()) {
            return 0;
        }

        // Obtener categorías existentes del usuario para evitar duplicados
        Map<String, Category> existingByName = new HashMap<>();
        for (Category existing : categoryRepository.findByIdUsuario(loggedInUserId)) {
            if (existing.isActive()) {
                existingByName.put(existing.getName().toLowerCase(), existing);
            }
        }

        int count = 0;
        for (Category cat : snapshot.categories()) {
            // Saltar categorías inactivas del snapshot
            if (!cat.isActive()) {
                log.debug("Categoría inactiva omitida: {}", cat.getName());
                continue;
            }

            String nameLower = cat.getName().toLowerCase();
            Category existing = existingByName.get(nameLower);
            
            if (existing != null) {
                // Reutilizar categoría existente: actualizar y mapear el ID antiguo al existente
                existing.setColor(cat.getColor());
                existing.setBudgetLimit(cat.getBudgetLimit());
                existing.setModifiedAt(java.time.LocalDateTime.now());
                categoryRepository.save(existing);
                categoryIdMap.put(cat.getId(), existing.getId());
                log.debug("Categoría reutilizada: {} -> {}", cat.getName(), existing.getId());
            } else {
                // Crear nueva categoría con UUID nuevo (nunca reutilizar el del snapshot)
                UUID newId = UUID.randomUUID();
                categoryIdMap.put(cat.getId(), newId);
                Category safeCat = new Category(
                        newId, loggedInUserId, cat.getName(), cat.getColor(),
                        cat.getBudgetLimit(), cat.getCreatedAt(), cat.getModifiedAt(), cat.isActive()
                );
                categoryRepository.save(safeCat);
                existingByName.put(nameLower, safeCat);
                count++;
            }
        }
        log.debug("Categorías importadas: {} (nuevas: {})", snapshot.categories().size(), count);
        return count;
    }

    private int importTransactions(UserDataSnapshot snapshot, UUID loggedInUserId, Map<UUID, UUID> categoryIdMap) {
        if (snapshot.transactions() == null || snapshot.transactions().isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Transaction tx : snapshot.transactions()) {
            // Saltar transacciones inactivas del snapshot
            if (!tx.isActive()) {
                continue;
            }

            UUID newCategoryId = null;
            if (tx.getCategoryId() != null) {
                newCategoryId = categoryIdMap.get(tx.getCategoryId());
                if (newCategoryId == null) {
                    log.warn("Transacción {} referencia categoría {} no encontrada en el mapa. Se importará sin categoría.",
                            tx.getId(), tx.getCategoryId());
                }
            }

            Transaction safeTx = new Transaction(
                    UUID.randomUUID(), loggedInUserId, newCategoryId, tx.getAmount(),
                    tx.getDate(), tx.getType(), tx.getDescription(),
                    tx.getCreatedAt(), tx.getModifiedAt(), tx.isActive()
            );
            transactionRepository.save(safeTx);
            count++;
        }
        log.debug("Transacciones: {}", count);
        return count;
    }

    private int importPlannedTransactions(UserDataSnapshot snapshot, UUID loggedInUserId, Map<UUID, UUID> categoryIdMap) {
        if (snapshot.plannedTransactions() == null || snapshot.plannedTransactions().isEmpty()) {
            return 0;
        }

        int count = 0;
        for (PlannedTransaction ptx : snapshot.plannedTransactions()) {
            // Saltar transacciones recurrentes inactivas del snapshot
            if (!ptx.isActive()) {
                continue;
            }

            UUID newCategoryId = null;
            if (ptx.getCategoryId() != null) {
                newCategoryId = categoryIdMap.get(ptx.getCategoryId());
                if (newCategoryId == null) {
                    log.warn("Transacción recurrente {} referencia categoría {} no encontrada en el mapa. Se importará sin categoría.",
                            ptx.getId(), ptx.getCategoryId());
                }
            }

            PlannedTransaction safePtx = new PlannedTransaction(
                    UUID.randomUUID(), loggedInUserId, newCategoryId, ptx.getAmount(),
                    ptx.getType(), ptx.getDescription(), ptx.getFrequencyType(),
                    ptx.getFrequencyInterval(), ptx.getStartDate(), ptx.getEndDate(),
                    ptx.getCreatedAt(), ptx.getModifiedAt(), ptx.isActive(), ptx.getLastExecutedDate()
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
            // Saltar inversiones inactivas del snapshot
            if (!inv.isActive()) {
                continue;
            }

            Investment safeInv = new Investment(
                    UUID.randomUUID(), loggedInUserId, inv.getAssetName(), inv.getTicker(),
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

    private int importSavingsGoals(UserDataSnapshot snapshot, UUID loggedInUserId) {
        if (snapshot.savingsGoals() == null || snapshot.savingsGoals().isEmpty()) {
            return 0;
        }

        int count = 0;
        for (SavingsGoal sg : snapshot.savingsGoals()) {
            if (!sg.isActive()) {
                continue;
            }

            SavingsGoal safeSg = new SavingsGoal(
                    UUID.randomUUID(), loggedInUserId, sg.getName(),
                    sg.getTargetAmount(), sg.getCurrentAmount(), sg.getDeadline(),
                    sg.getPriority(), sg.getStatus(), sg.getLink(),
                    sg.getCreatedAt(), sg.getModifiedAt(), sg.isActive()
            );
            savingsGoalRepository.save(safeSg);
            count++;
        }
        log.debug("Metas de ahorro: {}", count);
        return count;
    }

    private int importUserPreferences(UserDataSnapshot snapshot, UUID loggedInUserId) {
        if (snapshot.userPreferences() == null) {
            return 0;
        }

        UserPreferences prefs = snapshot.userPreferences();
        // Si ya existen preferencias para este usuario, las actualizamos
        UserPreferences existing = userPreferencesRepository.findByUserId(loggedInUserId).orElse(null);
        if (existing != null) {
            existing.setPreferencesJson(prefs.getPreferencesJson());
            existing.setModifiedAt(java.time.LocalDateTime.now());
            userPreferencesRepository.save(existing);
            log.debug("Preferencias de usuario actualizadas");
        } else {
            UserPreferences newPrefs = new UserPreferences(
                    UUID.randomUUID(), loggedInUserId, prefs.getPreferencesJson()
            );
            newPrefs.setCreatedAt(prefs.getCreatedAt());
            newPrefs.setModifiedAt(prefs.getModifiedAt());
            userPreferencesRepository.save(newPrefs);
            log.debug("Preferencias de usuario creadas");
        }
        return 1;
    }

    private int importGroupGoals(UserDataSnapshot snapshot, UUID loggedInUserId, Map<UUID, UUID> goalIdMap) {
        if (snapshot.goalUnits() == null || snapshot.goalUnits().isEmpty()) {
            return 0;
        }

        int count = 0;

        // ─── 1. Importar GoalUnits (regenerar UUIDs) ───
        for (GoalUnit goalUnit : snapshot.goalUnits()) {
            if (!goalUnit.isActive()) {
                continue;
            }

            UUID oldGoalId = goalUnit.getId();
            UUID newGoalId = UUID.randomUUID();
            goalIdMap.put(oldGoalId, newGoalId);

            GoalUnit safeGoalUnit = new GoalUnit();
            safeGoalUnit.setId(newGoalId);
            safeGoalUnit.setName(goalUnit.getName());
            safeGoalUnit.setMonthlyTarget(goalUnit.getMonthlyTarget());
            safeGoalUnit.setDistributionMode(goalUnit.getDistributionMode());
            safeGoalUnit.setCreatedAt(goalUnit.getCreatedAt());
            safeGoalUnit.setModifiedAt(goalUnit.getModifiedAt());
            safeGoalUnit.setActive(goalUnit.isActive());
            safeGoalUnit.setMembers(null); // Se reconstruyen después

            goalUnitRepository.save(safeGoalUnit);
            count++;
        }
        log.debug("GoalUnits importadas: {}", count);

        // ─── 2. Importar GoalMembers (mapear goalId) ───
        int memberCount = 0;
        if (snapshot.goalMembers() != null) {
            for (GoalMember gm : snapshot.goalMembers()) {
                if (!gm.isActive()) {
                    continue;
                }

                UUID newGoalId = goalIdMap.get(gm.getGoalId());
                if (newGoalId == null) {
                    log.warn("GoalMember {} referencia GoalUnit {} no encontrada en el mapa. Se omitirá.",
                            gm.getId(), gm.getGoalId());
                    continue;
                }

                GoalMember safeGm = new GoalMember();
                safeGm.setId(UUID.randomUUID());
                safeGm.setGoalId(newGoalId);
                safeGm.setUserId(loggedInUserId);
                safeGm.setRole(gm.getRole());
                safeGm.setSalary(gm.getSalary());
                safeGm.setCustomPercentage(gm.getCustomPercentage());
                safeGm.setJoinedAt(gm.getJoinedAt());
                safeGm.setCreatedAt(gm.getCreatedAt());
                safeGm.setModifiedAt(gm.getModifiedAt());
                safeGm.setActive(gm.isActive());

                goalMemberRepository.save(safeGm);
                memberCount++;
            }
        }
        log.debug("GoalMembers importados: {}", memberCount);
        count += memberCount;

        // ─── 3. Importar GoalContributions (mapear goalId) ───
        int contributionCount = 0;
        if (snapshot.goalContributions() != null) {
            for (GoalContribution gc : snapshot.goalContributions()) {
                if (!gc.isActive()) {
                    continue;
                }

                UUID newGoalId = goalIdMap.get(gc.getGoalId());
                if (newGoalId == null) {
                    log.warn("GoalContribution {} referencia GoalUnit {} no encontrada en el mapa. Se omitirá.",
                            gc.getId(), gc.getGoalId());
                    continue;
                }

                GoalContribution safeGc = new GoalContribution();
                safeGc.setId(UUID.randomUUID());
                safeGc.setGoalId(newGoalId);
                safeGc.setUserId(loggedInUserId);
                safeGc.setAmount(gc.getAmount());
                safeGc.setDate(gc.getDate());
                safeGc.setCreatedAt(gc.getCreatedAt());
                safeGc.setModifiedAt(gc.getModifiedAt());
                safeGc.setActive(gc.isActive());

                goalContributionRepository.save(safeGc);
                contributionCount++;
            }
        }
        log.debug("GoalContributions importadas: {}", contributionCount);
        count += contributionCount;

        return count;
    }

    // ─── Verificación post-importación ────────────────────────

    private void verifyImportedTransactions(UUID loggedInUserId, Map<UUID, UUID> categoryIdMap) {
        // Verificar que todas las transacciones importadas del usuario tengan categoría resoluble
        var allTransactions = transactionRepository.findAllByUserId(loggedInUserId);
        int orphanCount = 0;
        for (Transaction tx : allTransactions) {
            if (tx.getCategoryId() != null) {
                boolean resolvable = categoryRepository.findActiveByIdAndUserId(tx.getCategoryId(), loggedInUserId).isPresent();
                if (!resolvable) {
                    orphanCount++;
                    log.warn("Transacción {} tiene categoría {} no resoluble para el usuario {}",
                            tx.getId(), tx.getCategoryId(), loggedInUserId);
                }
            }
        }
        if (orphanCount > 0) {
            log.warn("{} transacciones tienen categorías no resueltas tras la importación", orphanCount);
        } else {
            log.debug("Todas las transacciones tienen categorías resolubles correctamente");
        }
    }
}
