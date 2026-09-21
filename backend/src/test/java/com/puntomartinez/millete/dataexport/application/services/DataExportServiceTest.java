package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.dataexport.domain.model.*;
import com.puntomartinez.millete.dataexport.domain.ports.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataExportService")
class DataExportServiceTest {

    @Mock
    private CategoryExportPort categoryExportPort;

    @Mock
    private TransactionExportPort transactionExportPort;

    @Mock
    private PlannedTransactionExportPort plannedTransactionExportPort;

    @Mock
    private InvestmentExportPort investmentExportPort;

    @Mock
    private SavingsGoalExportPort savingsGoalExportPort;

    @Mock
    private UserPreferencesExportPort userPreferencesExportPort;

    @Mock
    private FileZipExportPort fileZipExportPort;

    @Mock
    private FileCsvExportPort fileCsvExportPort;

    @Mock
    private FilePdfExportPort filePdfExportPort;

    @InjectMocks
    private DataExportService dataExportService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        // Mockito no procesa @Value, lo inyectamos manualmente para evitar NPE o datos nulos en el metadata
        ReflectionTestUtils.setField(dataExportService, "appVersion", "1.0.0");
    }

    private CategorySnapshot createCategorySnapshot() {
        return new CategorySnapshot(
                UUID.randomUUID(), userId, "Food", "#FF5733",
                new BigDecimal("500.00"),
                LocalDateTime.now(), LocalDateTime.now(), true
        );
    }

    private TransactionSnapshot createTransactionSnapshot(UUID categoryId) {
        return new TransactionSnapshot(
                UUID.randomUUID(), userId, categoryId,
                new BigDecimal("50.00"), LocalDateTime.now(),
                "EXPENSE", "Lunch",
                LocalDateTime.now(), LocalDateTime.now(), true
        );
    }

    @Test
    @DisplayName("exportAllUserData should return complete snapshot")
    void exportAllUserDataShouldReturnCompleteSnapshot() {
        CategorySnapshot cat = createCategorySnapshot();
        TransactionSnapshot tx = createTransactionSnapshot(cat.id());

        when(categoryExportPort.findByUserId(userId)).thenReturn(List.of(cat));
        when(transactionExportPort.findAllByUserId(userId)).thenReturn(List.of(tx));
        when(plannedTransactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesExportPort.findByUserId(userId)).thenReturn(Optional.empty());

        UserDataSnapshot result = dataExportService.exportAllUserData(userId);

        assertThat(result).isNotNull();
        assertThat(result.metadata()).isNotNull();
        assertThat(result.metadata().version())
                .isEqualTo(ExportVersion.CURRENT.toString());
        assertThat(result.metadata().appVersion()).isEqualTo("1.0.0");
        assertThat(result.categories()).hasSize(1);
        assertThat(result.transactions()).hasSize(1);
        assertThat(result.plannedTransactions()).isEmpty();
        assertThat(result.investments()).isEmpty();
        assertThat(result.savingsGoals()).isEmpty();
        assertThat(result.userPreferences()).isNull();
    }

    @Test
    @DisplayName("exportAllUserData should handle empty data")
    void exportAllUserDataShouldHandleEmptyData() {
        when(categoryExportPort.findByUserId(userId)).thenReturn(List.of());
        when(transactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesExportPort.findByUserId(userId)).thenReturn(Optional.empty());

        UserDataSnapshot result = dataExportService.exportAllUserData(userId);

        assertThat(result).isNotNull();
        assertThat(result.categories()).isEmpty();
        assertThat(result.transactions()).isEmpty();
        assertThat(result.userPreferences()).isNull();
    }

    @Test
    @DisplayName("buildExportData should return filtered active data")
    void buildExportDataShouldReturnFilteredActiveData() {
        CategorySnapshot cat = createCategorySnapshot();
        // Pasamos el ID de la categoría para que el servicio pueda resolver el nombre "Food"
        TransactionSnapshot tx = createTransactionSnapshot(cat.id());

        when(categoryExportPort.findByUserId(userId)).thenReturn(List.of(cat));
        when(transactionExportPort.findAllByUserId(userId)).thenReturn(List.of(tx));
        when(plannedTransactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesExportPort.findByUserId(userId)).thenReturn(Optional.empty());

        ExportData result = dataExportService.buildExportData(userId);

        assertThat(result.categories()).hasSize(1);
        assertThat(result.transactions()).hasSize(1);
        assertThat(result.transactions().getFirst().categoryName()).isEqualTo("Food");
    }

    @Test
    @DisplayName("buildExportData should filter inactive entities")
    void buildExportDataShouldFilterInactiveEntities() {
        CategorySnapshot activeCat = createCategorySnapshot();
        CategorySnapshot inactiveCat = new CategorySnapshot(
                UUID.randomUUID(), userId, "Inactive", "#00FF00",
                new BigDecimal("300.00"),
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        when(categoryExportPort.findByUserId(userId))
                .thenReturn(List.of(activeCat, inactiveCat));
        when(transactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesExportPort.findByUserId(userId)).thenReturn(Optional.empty());

        ExportData result = dataExportService.buildExportData(userId);

        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().getFirst().name()).isEqualTo("Food");
    }

    @Test
    @DisplayName("exportUserDataAsZip should delegate to file export port")
    void exportUserDataAsZipShouldDelegateToFileExportPort() {
        when(categoryExportPort.findByUserId(userId)).thenReturn(List.of());
        when(transactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesExportPort.findByUserId(userId)).thenReturn(Optional.empty());
        when(fileZipExportPort.generateZip(any(ExportData.class)))
                .thenReturn(new byte[]{1, 2, 3});

        byte[] result = dataExportService.exportUserDataAsZip(userId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        verify(fileZipExportPort).generateZip(any(ExportData.class));
    }

    @Test
    @DisplayName("exportUserDataAsCsv should delegate to file export port")
    void exportUserDataAsCsvShouldDelegateToFileExportPort() {
        when(categoryExportPort.findByUserId(userId)).thenReturn(List.of());
        when(transactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(plannedTransactionExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(investmentExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(userPreferencesExportPort.findByUserId(userId)).thenReturn(Optional.empty());
        when(fileCsvExportPort.generateCsv(any(ExportData.class), eq("categories")))
                .thenReturn(new byte[]{4, 5, 6});

        byte[] result = dataExportService.exportUserDataAsCsv(userId, "categories");

        assertThat(result).isNotNull();
        verify(fileCsvExportPort).generateCsv(any(ExportData.class), eq("categories"));
    }

    @Test
    @DisplayName("exportUserDataAsPdf should delegate to pdf file export port")
    void exportUserDataAsPdfShouldDelegateToPdfFileExportPort() {
        when(categoryExportPort.findByUserId(userId)).thenReturn(List.of());
        // buildPdfExportData usa findByUserIdAndDateBetween, NO findAllByUserId
        when(transactionExportPort.findByUserIdAndDateBetween(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of());
        when(investmentExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(filePdfExportPort.generatePdf(any(PdfExportData.class)))
                .thenReturn(new byte[]{7, 8, 9});

        byte[] result = dataExportService.exportUserDataAsPdf(userId, PeriodType.ONE_MONTH);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        verify(filePdfExportPort).generatePdf(any(PdfExportData.class));
    }

    @Test
    @DisplayName("buildPdfExportData should calculate metrics")
    void buildPdfExportDataShouldCalculateMetrics() {
        CategorySnapshot cat = createCategorySnapshot();
        TransactionSnapshot income = new TransactionSnapshot(
                UUID.randomUUID(), userId, UUID.randomUUID(),
                new BigDecimal("1000.00"), LocalDateTime.now(),
                "INCOME", "Salary",
                LocalDateTime.now(), LocalDateTime.now(), true
        );
        TransactionSnapshot expense = new TransactionSnapshot(
                UUID.randomUUID(), userId, cat.id(),
                new BigDecimal("300.00"), LocalDateTime.now(),
                "EXPENSE", "Groceries",
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        when(categoryExportPort.findByUserId(userId)).thenReturn(List.of(cat));
        when(transactionExportPort.findByUserIdAndDateBetween(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(income, expense));
        when(investmentExportPort.findAllByUserId(userId)).thenReturn(List.of());
        when(savingsGoalExportPort.findAllByUserId(userId)).thenReturn(List.of());

        PdfExportData result = dataExportService.buildPdfExportData(userId, PeriodType.ONE_MONTH);

        assertThat(result).isNotNull();
        assertThat(result.periodDisplayName()).isEqualTo("1 month");
        assertThat(result.summary()).isNotNull();
        assertThat(result.summary().balance()).isEqualByComparingTo("700.00");
        assertThat(result.summary().totalIncome()).isEqualByComparingTo("1000.00");
        assertThat(result.summary().totalExpenses()).isEqualByComparingTo("300.00");
        assertThat(result.summary().transactionCount()).isEqualTo(2);
        assertThat(result.summary().topCategoryName()).isEqualTo("Food");
    }
}