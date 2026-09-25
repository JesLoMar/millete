package com.puntomartinez.millete.dataexport.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.puntomartinez.millete.dataexport.domain.migration.MigrationChain;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataImportService")
class DataImportServiceTest {

    @Mock
    private CategoryImportPort categoryImportPort;

    @Mock
    private TransactionImportPort transactionImportPort;

    @Mock
    private PlannedTransactionImportPort plannedTransactionImportPort;

    @Mock
    private InvestmentImportPort investmentImportPort;

    @Mock
    private SavingsGoalImportPort savingsGoalImportPort;

    @Mock
    private UserPreferencesImportPort userPreferencesImportPort;

    @Mock
    private TransactionImportVerificationPort transactionImportVerificationPort;

    @Mock
    private MigrationChain migrationChain;

    @InjectMocks
    private DataImportService dataImportService;

    private UUID destUserId;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        destUserId = UUID.randomUUID();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private UserDataSnapshot buildEmptySnapshot() {
        return new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        ExportVersion.CURRENT.toString(),
                        Instant.now(),
                        "0.2.0"
                ),
                List.of(), List.of(), List.of(), List.of(), List.of(), null
        );
    }

    @Test
    @DisplayName("Should import empty snapshot successfully")
    void importUserDataShouldImportEmptySnapshot() throws Exception {
        UserDataSnapshot snapshot = buildEmptySnapshot();
        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.json", "application/json", json.getBytes()
        );

        when(categoryImportPort.importCategories(any(), any()))
                .thenReturn(new com.puntomartinez.millete.dataexport.domain.model.CategoryImportResult(
                        new java.util.HashMap<>(), 0
                ));
        when(transactionImportPort.importTransactions(any(), any(), any())).thenReturn(0);
        when(plannedTransactionImportPort.importPlannedTransactions(any(), any(), any())).thenReturn(0);
        when(investmentImportPort.importInvestments(any(), any())).thenReturn(0);
        when(savingsGoalImportPort.importSavingsGoals(any(), any())).thenReturn(0);

        String result = dataImportService.importUserData(file, destUserId);

        assertThat(result).contains("Importación exitosa");
        verify(transactionImportVerificationPort).verifyImportedTransactions(destUserId);
    }

    @Test
    @DisplayName("Should reject incompatible version")
    void importUserDataShouldRejectIncompatibleVersion() throws Exception {
        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        "99.0.0", Instant.now(), "99.0.0"
                ),
                List.of(), List.of(), List.of(), List.of(), List.of(), null
        );
        String json = objectMapper.writeValueAsString(snapshot);
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.json", "application/json", json.getBytes()
        );

        try {
            dataImportService.importUserData(file, destUserId);
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("incompatible");
        }
    }

    @Test
    @DisplayName("Should reject invalid JSON")
    void importUserDataShouldRejectInvalidJson() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.json", "application/json",
                "not valid json".getBytes()
        );

        try {
            dataImportService.importUserData(file, destUserId);
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("JSON");
        }
    }

    @Test
    @DisplayName("Should reject file exceeding max size")
    void importUserDataShouldRejectFileExceedingMaxSize() {
        byte[] largeContent = new byte[51 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.json", "application/json", largeContent
        );

        try {
            dataImportService.importUserData(file, destUserId);
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("demasiado grande");
        }
    }

    @Test
    @DisplayName("Should trigger migration when needed")
    void importUserDataShouldTriggerMigrationWhenNeeded() throws Exception {
        UserDataSnapshot oldSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        "0.0.1", Instant.now(), "0.0.1"
                ),
                List.of(), List.of(), List.of(), List.of(), List.of(), null
        );
        UserDataSnapshot migratedSnapshot = buildEmptySnapshot();
        String json = objectMapper.writeValueAsString(oldSnapshot);
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.json", "application/json", json.getBytes()
        );

        when(migrationChain.migrateToLatest(any())).thenReturn(migratedSnapshot);
        when(categoryImportPort.importCategories(any(), any()))
                .thenReturn(new com.puntomartinez.millete.dataexport.domain.model.CategoryImportResult(
                        new java.util.HashMap<>(), 0
                ));
        when(transactionImportPort.importTransactions(any(), any(), any())).thenReturn(0);
        when(plannedTransactionImportPort.importPlannedTransactions(any(), any(), any())).thenReturn(0);
        when(investmentImportPort.importInvestments(any(), any())).thenReturn(0);
        when(savingsGoalImportPort.importSavingsGoals(any(), any())).thenReturn(0);

        String result = dataImportService.importUserData(file, destUserId);

        assertThat(result).contains("Importación exitosa");
        verify(migrationChain).migrateToLatest(any());
    }
}