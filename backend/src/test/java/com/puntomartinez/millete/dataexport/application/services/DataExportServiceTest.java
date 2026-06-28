package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.model.*;
import com.puntomartinez.millete.dataexport.domain.ports.out.FileExportPort;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;
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
    @Mock
    private FileExportPort fileExportPort;
    @Mock
    private FileExportPort pdfFileExportPort;

    private DataExportService dataExportService;

    private UUID userId;
    private UUID categoryId;
    private UUID goalId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        goalId = UUID.randomUUID();
        dataExportService = new DataExportService(
                categoryRepository,
                transactionRepository,
                plannedTransactionRepository,
                investmentRepository,
                savingsGoalRepository,
                userPreferencesRepository,
                goalUnitRepository,
                goalMemberRepository,
                goalContributionRepository,
                fileExportPort,
                pdfFileExportPort
        );
    }

    private Category createCategory() {
        Category cat = new Category();
        cat.setId(categoryId);
        cat.setUserId(userId);
        cat.setName("Comida");
        cat.setBudgetLimit(new BigDecimal("500.00"));
        cat.setActive(true);
        return cat;
    }

    private Transaction createTransaction() {
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setUserId(userId);
        tx.setCategoryId(categoryId);
        tx.setAmount(new BigDecimal("50.00"));
        tx.setDate(LocalDateTime.now());
        tx.setType(Transaction.TransactionType.EXPENSE);
        tx.setDescription("Almuerzo");
        tx.setActive(true);
        return tx;
    }

    private PlannedTransaction createPlannedTransaction() {
        PlannedTransaction ptx = new PlannedTransaction();
        ptx.setId(UUID.randomUUID());
        ptx.setUserId(userId);
        ptx.setCategoryId(categoryId);
        ptx.setAmount(new BigDecimal("100.00"));
        ptx.setType(Transaction.TransactionType.EXPENSE);
        ptx.setDescription("Alquiler");
        ptx.setFrequencyType(PlannedTransaction.FrequencyType.MONTHS);
        ptx.setFrequencyInterval(1);
        ptx.setStartDate(LocalDate.now());
        ptx.setActive(true);
        return ptx;
    }

    private Investment createInvestment() {
        Investment inv = new Investment();
        inv.setId(UUID.randomUUID());
        inv.setUserId(userId);
        inv.setAssetName("Apple");
        inv.setTicker("AAPL");
        inv.setQuantity(new BigDecimal("10"));
        inv.setPurchasePrice(new BigDecimal("150.00"));
        inv.setCurrentPrice(new BigDecimal("180.00"));
        inv.setType(Investment.InvestmentType.STOCK);
        inv.setPurchaseDate(LocalDateTime.now().minusMonths(3));
        inv.setActive(true);
        return inv;
    }

    private SavingsGoal createSavingsGoal() {
        SavingsGoal sg = new SavingsGoal();
        sg.setId(UUID.randomUUID());
        sg.setUserId(userId);
        sg.setName("Vacaciones");
        sg.setTargetAmount(new BigDecimal("2000.00"));
        sg.setCurrentAmount(new BigDecimal("500.00"));
        sg.setDeadline(LocalDate.now().plusMonths(6));
        sg.setPriority("HIGH");
        sg.setStatus("ACTIVE");
        sg.setActive(true);
        return sg;
    }

    private GoalUnit createGoalUnit() {
        GoalUnit gu = new GoalUnit();
        gu.setId(goalId);
        gu.setName("Viaje familiar");
        gu.setMonthlyTarget(new BigDecimal("300.00"));
        gu.setActive(true);
        return gu;
    }

    private GoalMember createGoalMember() {
        GoalMember gm = new GoalMember();
        gm.setId(UUID.randomUUID());
        gm.setGoalId(goalId);
        gm.setUserId(userId);
        gm.setRole(GoalRole.ADMIN);
        gm.setActive(true);
        return gm;
    }

    private GoalContribution createGoalContribution() {
        GoalContribution gc = new GoalContribution();
        gc.setId(UUID.randomUUID());
        gc.setGoalId(goalId);
        gc.setUserId(userId);
        gc.setAmount(new BigDecimal("100.00"));
        gc.setActive(true);
        return gc;
    }

    @Test
    void exportAllUserData_shouldReturnCompleteSnapshot() {
        Category cat = createCategory();
        Transaction tx = createTransaction();
        PlannedTransaction ptx = createPlannedTransaction();
        Investment inv = createInvestment();
        SavingsGoal sg = createSavingsGoal();
        UserPreferences prefs = new UserPreferences(UUID.randomUUID(), userId, "{\"theme\":\"dark\"}");
        GoalUnit gu = createGoalUnit();
        GoalMember gm = createGoalMember();
        GoalContribution gc = createGoalContribution();

        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of(cat));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(tx));
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of(ptx));
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of(inv));
        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of(sg));
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.of(prefs));
        when(goalMemberRepository.findByUserId(userId)).thenReturn(List.of(gm));
        when(goalUnitRepository.findById(goalId)).thenReturn(Optional.of(gu));
        when(goalMemberRepository.findByGoalId(goalId)).thenReturn(List.of(gm));
        when(goalContributionRepository.findByGoalId(goalId)).thenReturn(List.of(gc));

        UserDataSnapshot result = dataExportService.exportAllUserData(userId);

        assertNotNull(result);
        assertNotNull(result.metadata());
        assertEquals(ExportVersion.CURRENT.toString(), result.metadata().version());
        assertEquals(1, result.categories().size());
        assertEquals(1, result.transactions().size());
        assertEquals(1, result.plannedTransactions().size());
        assertEquals(1, result.investments().size());
        assertEquals(1, result.savingsGoals().size());
        assertNotNull(result.userPreferences());
        assertEquals(1, result.goalUnits().size());
        assertEquals(1, result.goalMembers().size());
        assertEquals(1, result.goalContributions().size());
    }

    @Test
    void exportAllUserData_shouldHandleEmptyData() {
        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of());
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(goalMemberRepository.findByUserId(userId)).thenReturn(List.of());

        UserDataSnapshot result = dataExportService.exportAllUserData(userId);

        assertNotNull(result);
        assertTrue(result.categories().isEmpty());
        assertTrue(result.transactions().isEmpty());
        assertNull(result.userPreferences());
        assertTrue(result.goalUnits().isEmpty());
    }

    @Test
    void buildExportData_shouldReturnFilteredActiveData() {
        Category cat = createCategory();
        Transaction tx = createTransaction();
        tx.setCategoryId(categoryId);
        SavingsGoal sg = createSavingsGoal();

        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of(cat));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(tx));
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of(sg));
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(goalMemberRepository.findByUserId(userId)).thenReturn(List.of());

        ExportData result = dataExportService.buildExportData(userId);

        assertEquals(1, result.categories().size());
        assertEquals(1, result.transactions().size());
        assertEquals(1, result.savingsGoals().size());
        assertEquals("Comida", result.transactions().getFirst().categoryName());
    }

    @Test
    void buildExportData_shouldFilterInactiveEntities() {
        Category activeCat = createCategory();
        Category inactiveCat = createCategory();
        inactiveCat.setId(UUID.randomUUID());
        inactiveCat.setActive(false);

        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of(activeCat, inactiveCat));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(goalMemberRepository.findByUserId(userId)).thenReturn(List.of());

        ExportData result = dataExportService.buildExportData(userId);

        assertEquals(1, result.categories().size());
        assertEquals("Comida", result.categories().getFirst().name());
    }

    @Test
    void buildPdfExportData_shouldCalculateMetrics() {
        Category cat = createCategory();
        Transaction income = createTransaction();
        income.setType(Transaction.TransactionType.INCOME);
        income.setAmount(new BigDecimal("1000.00"));
        Transaction expense = createTransaction();
        expense.setType(Transaction.TransactionType.EXPENSE);
        expense.setAmount(new BigDecimal("300.00"));
        expense.setCategoryId(categoryId);
        Investment inv = createInvestment();
        SavingsGoal sg = createSavingsGoal();

        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of(cat));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(income, expense));
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of(inv));
        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of(sg));
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(goalMemberRepository.findByUserId(userId)).thenReturn(List.of());

        PdfExportData result = dataExportService.buildPdfExportData(userId, PeriodType.ONE_MONTH);

        assertNotNull(result);
        assertEquals("1 month", result.periodDisplayName());
        assertNotNull(result.summary());
        assertEquals(new BigDecimal("700.00"), result.summary().balance());
        assertEquals(new BigDecimal("1000.00"), result.summary().totalIncome());
        assertEquals(new BigDecimal("300.00"), result.summary().totalExpenses());
        assertEquals(2, result.summary().transactionCount());
        assertEquals("Comida", result.summary().topCategoryName());
        assertEquals(1, result.investments().size());
        assertEquals(1, result.savingsGoals().size());
    }

    @Test
    void exportUserDataAsZip_shouldDelegateToFileExportPort() {
        Category cat = createCategory();
        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of(cat));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(goalMemberRepository.findByUserId(userId)).thenReturn(List.of());
        when(fileExportPort.generateZip(any(ExportData.class))).thenReturn(new byte[]{1, 2, 3});

        byte[] result = dataExportService.exportUserDataAsZip(userId);

        assertNotNull(result);
        assertEquals(3, result.length);
        verify(fileExportPort).generateZip(any(ExportData.class));
    }

    @Test
    void exportUserDataAsCsv_shouldDelegateToFileExportPort() {
        Category cat = createCategory();
        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of(cat));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(goalMemberRepository.findByUserId(userId)).thenReturn(List.of());
        when(fileExportPort.generateCsv(any(ExportData.class), eq("categories"))).thenReturn(new byte[]{4, 5, 6});

        byte[] result = dataExportService.exportUserDataAsCsv(userId, "categories");

        assertNotNull(result);
        verify(fileExportPort).generateCsv(any(ExportData.class), eq("categories"));
    }

    @Test
    void exportUserDataAsPdf_shouldDelegateToPdfFileExportPort() {
        Category cat = createCategory();
        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of(cat));
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(goalMemberRepository.findByUserId(userId)).thenReturn(List.of());
        when(pdfFileExportPort.generatePdf(any(PdfExportData.class))).thenReturn(new byte[]{7, 8, 9});

        byte[] result = dataExportService.exportUserDataAsPdf(userId, PeriodType.ONE_MONTH);

        assertNotNull(result);
        assertEquals(3, result.length);
        verify(pdfFileExportPort).generatePdf(any(PdfExportData.class));
    }
}
