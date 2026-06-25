package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.migration.MigrationChain;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalMemberRepository;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataImportServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @SuppressWarnings("unused")
    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;
    @SuppressWarnings("unused")
    @Mock
    private InvestmentRepository investmentRepository;
    @Mock
    private SavingsGoalRepository savingsGoalRepository;
    @Mock
    private UserPreferencesRepository userPreferencesRepository;
    @Mock
    private GoalUnitRepository goalUnitRepository;
    @Mock
    private GoalMemberRepository goalMemberRepository;
    @Mock
    private GoalContributionRepository goalContributionRepository;
    @SuppressWarnings("unused")
    @Mock
    private MigrationChain migrationChain;

    @InjectMocks
    private DataImportService dataImportService;

    private UUID sourceUserId;
    private UUID destUserId;
    private UUID sourceCategoryId;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sourceUserId = UUID.randomUUID();
        destUserId = UUID.randomUUID();
        sourceCategoryId = UUID.randomUUID();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private UserDataSnapshot buildSnapshot(Category category, Transaction transaction) {
        return new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                category != null ? List.of(category) : List.of(),
                transaction != null ? List.of(transaction) : List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                null,
                null
        );
    }

    @Test
    void importUserData_shouldRemapCategoryIds_whenImportingToDifferentUser() throws Exception {
        // Arrange: crear snapshot con 1 categoría y 1 transacción
        Category sourceCategory = new Category(
                sourceCategoryId, sourceUserId, "Comida", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        Transaction sourceTransaction = new Transaction(
                UUID.randomUUID(), sourceUserId, sourceCategoryId,
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Almuerzo",
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        UserDataSnapshot snapshot = buildSnapshot(sourceCategory, sourceTransaction);

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        // Mock: no hay categorías existentes para el usuario destino
        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));

        // Verificar que la categoría guardada tiene userId del destino y UUID nuevo
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertEquals(destUserId, savedCategory.getUserId());
        assertNotEquals(sourceCategoryId, savedCategory.getId()); // UUID nuevo

        // Verificar que la transacción guardada apunta a la nueva categoría y userId del destino
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        Transaction savedTx = txCaptor.getValue();
        assertEquals(destUserId, savedTx.getUserId());
        assertNotEquals(sourceCategoryId, savedTx.getCategoryId()); // ID remapeado
        assertNotNull(savedTx.getCategoryId()); // No debe ser null
    }

    @Test
    void importUserData_shouldReuseExistingCategory_whenNameMatches() throws Exception {
        // Arrange: usuario destino ya tiene categoría "Comida"
        UUID existingCategoryId = UUID.randomUUID();
        Category existingCategory = new Category(
                existingCategoryId, destUserId, "Comida", "#00FF00",
                new BigDecimal("300.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        Category sourceCategory = new Category(
                sourceCategoryId, sourceUserId, "Comida", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        Transaction sourceTransaction = new Transaction(
                UUID.randomUUID(), sourceUserId, sourceCategoryId,
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Almuerzo",
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        UserDataSnapshot snapshot = buildSnapshot(sourceCategory, sourceTransaction);

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(List.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));

        // Verificar que NO se creó nueva categoría (solo se actualizó la existente)
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertEquals(existingCategoryId, savedCategory.getId()); // Mismo ID existente

        // Verificar que la transacción apunta al ID existente
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        Transaction savedTx = txCaptor.getValue();
        assertEquals(existingCategoryId, savedTx.getCategoryId());
    }

    @Test
    void importUserData_shouldSkipInactiveEntities() throws Exception {
        // Arrange: categoría inactiva y transacción inactiva
        Category inactiveCategory = new Category(
                sourceCategoryId, sourceUserId, "Inactiva", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), false
        );

        Transaction inactiveTransaction = new Transaction(
                UUID.randomUUID(), sourceUserId, sourceCategoryId,
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Inactiva",
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        UserDataSnapshot snapshot = buildSnapshot(inactiveCategory, inactiveTransaction);

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));
        assertTrue(result.contains("0 registros importados")); // Nada se importó

        // Verificar que NO se guardó nada
        verify(categoryRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void importUserData_shouldSanitizeUserIds() throws Exception {
        // Arrange: snapshot con userId de origen
        Category sourceCategory = new Category(
                sourceCategoryId, sourceUserId, "Comida", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        UserDataSnapshot snapshot = buildSnapshot(sourceCategory, null);

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        dataImportService.importUserData(file, destUserId);

        // Assert: verificar que la categoría guardada tiene userId del destino
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        assertEquals(destUserId, categoryCaptor.getValue().getUserId());
        assertNotEquals(sourceUserId, categoryCaptor.getValue().getUserId());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Tests para nuevas entidades v0.1.0
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void importUserData_shouldImportSavingsGoals_withNewUuids() throws Exception {
        // Arrange: snapshot con 1 savings goal
        UUID sourceGoalId = UUID.randomUUID();
        SavingsGoal sourceGoal = new SavingsGoal(
                sourceGoalId, sourceUserId, "Vacaciones",
                new BigDecimal("2000.00"), new BigDecimal("500.00"),
                LocalDate.now().plusMonths(6), "HIGH", "ACTIVE", null,
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(),
                List.of(sourceGoal),
                null, null, null, null
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));

        ArgumentCaptor<SavingsGoal> goalCaptor = ArgumentCaptor.forClass(SavingsGoal.class);
        verify(savingsGoalRepository, times(1)).save(goalCaptor.capture());
        SavingsGoal savedGoal = goalCaptor.getValue();
        assertEquals(destUserId, savedGoal.getUserId());
        assertNotEquals(sourceGoalId, savedGoal.getId()); // UUID nuevo
        assertEquals("Vacaciones", savedGoal.getName());
        assertEquals(new BigDecimal("2000.00"), savedGoal.getTargetAmount());
    }

    @Test
    void importUserData_shouldSkipInactiveSavingsGoals() throws Exception {
        // Arrange: snapshot con 1 savings goal inactivo
        SavingsGoal inactiveGoal = new SavingsGoal(
                UUID.randomUUID(), sourceUserId, "Inactivo",
                new BigDecimal("1000.00"), BigDecimal.ZERO,
                LocalDate.now().plusMonths(3), "MEDIUM", "CANCELLED", null,
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(),
                List.of(inactiveGoal),
                null, null, null, null
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("0 registros importados"));
        verify(savingsGoalRepository, never()).save(any());
    }

    @Test
    void importUserData_shouldImportUserPreferences_whenNotExisting() throws Exception {
        // Arrange: snapshot con preferencias
        UserPreferences sourcePrefs = new UserPreferences(
                UUID.randomUUID(), sourceUserId, "{\"theme\":\"dark\"}"
        );
        sourcePrefs.setCreatedAt(LocalDateTime.now());
        sourcePrefs.setModifiedAt(LocalDateTime.now());

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                sourcePrefs, null, null, null
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(userPreferencesRepository.findByUserId(destUserId)).thenReturn(Optional.empty());
        when(userPreferencesRepository.save(any(UserPreferences.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));

        ArgumentCaptor<UserPreferences> prefsCaptor = ArgumentCaptor.forClass(UserPreferences.class);
        verify(userPreferencesRepository, times(1)).save(prefsCaptor.capture());
        UserPreferences savedPrefs = prefsCaptor.getValue();
        assertEquals(destUserId, savedPrefs.getUserId());
        assertEquals("{\"theme\":\"dark\"}", savedPrefs.getPreferencesJson());
    }

    @Test
    void importUserData_shouldUpdateUserPreferences_whenExisting() throws Exception {
        // Arrange: usuario destino ya tiene preferencias
        UUID existingPrefsId = UUID.randomUUID();
        UserPreferences existingPrefs = new UserPreferences(
                existingPrefsId, destUserId, "{\"theme\":\"light\"}"
        );

        UserPreferences sourcePrefs = new UserPreferences(
                UUID.randomUUID(), sourceUserId, "{\"theme\":\"dark\"}"
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                sourcePrefs, null, null, null
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(userPreferencesRepository.findByUserId(destUserId)).thenReturn(Optional.of(existingPrefs));
        when(userPreferencesRepository.save(any(UserPreferences.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));

        ArgumentCaptor<UserPreferences> prefsCaptor = ArgumentCaptor.forClass(UserPreferences.class);
        verify(userPreferencesRepository, times(1)).save(prefsCaptor.capture());
        UserPreferences savedPrefs = prefsCaptor.getValue();
        assertEquals(existingPrefsId, savedPrefs.getId()); // Mismo ID
        assertEquals(destUserId, savedPrefs.getUserId());
        assertEquals("{\"theme\":\"dark\"}", savedPrefs.getPreferencesJson());
    }

    @Test
    void importUserData_shouldImportGroupGoals_withRemappedGoalIds() throws Exception {
        // Arrange: snapshot con 1 GoalUnit, 1 GoalMember y 1 GoalContribution
        UUID sourceGoalId = UUID.randomUUID();
        UUID sourceMemberId = UUID.randomUUID();
        UUID sourceContributionId = UUID.randomUUID();

        GoalUnit sourceGoalUnit = new GoalUnit();
        sourceGoalUnit.setId(sourceGoalId);
        sourceGoalUnit.setName("Viaje familiar");
        sourceGoalUnit.setMonthlyTarget(new BigDecimal("300.00"));
        sourceGoalUnit.setDistributionMode(DistributionMode.EQUITATIVE);
        sourceGoalUnit.setCreatedAt(LocalDateTime.now());
        sourceGoalUnit.setModifiedAt(LocalDateTime.now());
        sourceGoalUnit.setActive(true);

        GoalMember sourceMember = new GoalMember();
        sourceMember.setId(sourceMemberId);
        sourceMember.setGoalId(sourceGoalId);
        sourceMember.setUserId(sourceUserId);
        sourceMember.setRole(GoalRole.ADMIN);
        sourceMember.setSalary(new BigDecimal("2000.00"));
        sourceMember.setJoinedAt(LocalDateTime.now());
        sourceMember.setCreatedAt(LocalDateTime.now());
        sourceMember.setModifiedAt(LocalDateTime.now());
        sourceMember.setActive(true);

        GoalContribution sourceContribution = new GoalContribution();
        sourceContribution.setId(sourceContributionId);
        sourceContribution.setGoalId(sourceGoalId);
        sourceContribution.setUserId(sourceUserId);
        sourceContribution.setAmount(new BigDecimal("100.00"));
        sourceContribution.setDate(LocalDateTime.now());
        sourceContribution.setCreatedAt(LocalDateTime.now());
        sourceContribution.setModifiedAt(LocalDateTime.now());
        sourceContribution.setActive(true);

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null,
                List.of(sourceGoalUnit),
                List.of(sourceMember),
                List.of(sourceContribution)
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(goalUnitRepository.save(any(GoalUnit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalMemberRepository.save(any(GoalMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalContributionRepository.save(any(GoalContribution.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));

        // Verificar GoalUnit guardada con UUID nuevo
        ArgumentCaptor<GoalUnit> goalCaptor = ArgumentCaptor.forClass(GoalUnit.class);
        verify(goalUnitRepository, times(1)).save(goalCaptor.capture());
        GoalUnit savedGoal = goalCaptor.getValue();
        assertNotEquals(sourceGoalId, savedGoal.getId()); // UUID nuevo
        assertEquals("Viaje familiar", savedGoal.getName());

        // Verificar GoalMember guardado con goalId remapeado y userId del destino
        ArgumentCaptor<GoalMember> memberCaptor = ArgumentCaptor.forClass(GoalMember.class);
        verify(goalMemberRepository, times(1)).save(memberCaptor.capture());
        GoalMember savedMember = memberCaptor.getValue();
        assertNotEquals(sourceMemberId, savedMember.getId()); // UUID nuevo
        assertNotEquals(sourceGoalId, savedMember.getGoalId()); // goalId remapeado
        assertEquals(destUserId, savedMember.getUserId()); // userId sanitizado
        assertEquals(GoalRole.ADMIN, savedMember.getRole());

        // Verificar GoalContribution guardada con goalId remapeado y userId del destino
        ArgumentCaptor<GoalContribution> contribCaptor = ArgumentCaptor.forClass(GoalContribution.class);
        verify(goalContributionRepository, times(1)).save(contribCaptor.capture());
        GoalContribution savedContribution = contribCaptor.getValue();
        assertNotEquals(sourceContributionId, savedContribution.getId()); // UUID nuevo
        assertNotEquals(sourceGoalId, savedContribution.getGoalId()); // goalId remapeado
        assertEquals(destUserId, savedContribution.getUserId()); // userId sanitizado
        assertEquals(new BigDecimal("100.00"), savedContribution.getAmount());
    }

    @Test
    void importUserData_shouldSkipInactiveGroupGoals() throws Exception {
        // Arrange: snapshot con GoalUnit inactiva
        GoalUnit inactiveGoalUnit = new GoalUnit();
        inactiveGoalUnit.setId(UUID.randomUUID());
        inactiveGoalUnit.setName("Inactiva");
        inactiveGoalUnit.setMonthlyTarget(new BigDecimal("100.00"));
        inactiveGoalUnit.setDistributionMode(DistributionMode.EQUITATIVE);
        inactiveGoalUnit.setActive(false);

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null,
                List.of(inactiveGoalUnit),
                null, null
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("0 registros importados"));
        verify(goalUnitRepository, never()).save(any());
        verify(goalMemberRepository, never()).save(any());
        verify(goalContributionRepository, never()).save(any());
    }

    @Test
    void importUserData_shouldSkipGroupGoalMembersWithOrphanGoalId() throws Exception {
        // Arrange: snapshot con GoalMember que referencia un GoalId no presente en el snapshot
        UUID orphanGoalId = UUID.randomUUID();

        GoalMember orphanMember = new GoalMember();
        orphanMember.setId(UUID.randomUUID());
        orphanMember.setGoalId(orphanGoalId); // No existe en goalUnits
        orphanMember.setUserId(sourceUserId);
        orphanMember.setRole(GoalRole.MEMBER);
        orphanMember.setActive(true);

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null,
                null, // Sin goalUnits
                List.of(orphanMember),
                null
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));
        verify(goalMemberRepository, never()).save(any()); // Se omite porque no hay goalUnit
    }

    @Test
    void importUserData_shouldHandleSnapshotWithoutNewEntities() throws Exception {
        // Arrange: snapshot v0.0.1 (sin userPreferences, goalUnits, goalMembers, goalContributions)
        Category sourceCategory = new Category(
                sourceCategoryId, sourceUserId, "Comida", "#FF5733",
                new BigDecimal("500.00"), LocalDateTime.now(), LocalDateTime.now(), true
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(ExportVersion.CURRENT.toString(), LocalDateTime.now(), "0.1.0"),
                List.of(sourceCategory),
                List.of(), List.of(), List.of(), List.of(),
                null, null, null, null
        );

        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", json.getBytes());

        when(categoryRepository.findByIdUsuario(destUserId)).thenReturn(new ArrayList<>());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.findAllByUserId(destUserId)).thenReturn(new ArrayList<>());

        // Act
        String result = dataImportService.importUserData(file, destUserId);

        // Assert
        assertTrue(result.contains("Importación exitosa"));
        assertTrue(result.contains("1 registros importados")); // Solo la categoría

        verify(savingsGoalRepository, never()).save(any());
        verify(userPreferencesRepository, never()).save(any());
        verify(goalUnitRepository, never()).save(any());
        verify(goalMemberRepository, never()).save(any());
        verify(goalContributionRepository, never()).save(any());
    }
}
