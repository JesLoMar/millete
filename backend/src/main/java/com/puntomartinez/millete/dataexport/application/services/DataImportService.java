package com.puntomartinez.millete.dataexport.application.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.puntomartinez.millete.dataexport.domain.migration.MigrationChain;
import com.puntomartinez.millete.dataexport.domain.model.CategoryImportResult;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.CategoryImportPort;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentImportPort;
import com.puntomartinez.millete.dataexport.domain.ports.out.PlannedTransactionImportPort;
import com.puntomartinez.millete.dataexport.domain.ports.out.SavingsGoalImportPort;
import com.puntomartinez.millete.dataexport.domain.ports.out.TransactionImportPort;
import com.puntomartinez.millete.dataexport.domain.ports.out.TransactionImportVerificationPort;
import com.puntomartinez.millete.dataexport.domain.ports.out.UserPreferencesImportPort;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class DataImportService {

    private static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024;

    private final CategoryImportPort categoryImportPort;
    private final TransactionImportPort transactionImportPort;
    private final PlannedTransactionImportPort plannedTransactionImportPort;
    private final InvestmentImportPort investmentImportPort;
    private final SavingsGoalImportPort savingsGoalImportPort;
    private final UserPreferencesImportPort userPreferencesImportPort;
    private final TransactionImportVerificationPort transactionImportVerificationPort;
    private final MigrationChain migrationChain;
    private final ObjectMapper objectMapper;

    public DataImportService(
            CategoryImportPort categoryImportPort,
            TransactionImportPort transactionImportPort,
            PlannedTransactionImportPort plannedTransactionImportPort,
            InvestmentImportPort investmentImportPort,
            SavingsGoalImportPort savingsGoalImportPort,
            UserPreferencesImportPort userPreferencesImportPort,
            TransactionImportVerificationPort transactionImportVerificationPort,
            MigrationChain migrationChain
    ) {
        this.categoryImportPort = categoryImportPort;
        this.transactionImportPort = transactionImportPort;
        this.plannedTransactionImportPort =
                plannedTransactionImportPort;
        this.investmentImportPort =
                investmentImportPort;
        this.savingsGoalImportPort =
                savingsGoalImportPort;
        this.userPreferencesImportPort = userPreferencesImportPort;
        this.transactionImportVerificationPort =
                transactionImportVerificationPort;
        this.migrationChain = migrationChain;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public String importUserData(
            MultipartFile file,
            UUID loggedInUserId
    ) {
        validateFileSize(file);

        try (InputStream inputStream = file.getInputStream()) {
            log.debug("Leyendo archivo de importación...");

            UserDataSnapshot snapshot;
            try {
                snapshot = objectMapper.readValue(
                        inputStream,
                        UserDataSnapshot.class
                );
            } catch (MismatchedInputException e) {
                throw new InvalidInputException(
                        "El archivo tiene una estructura inesperada. "
                                + "Asegúrate de exportar desde esta aplicación."
                );
            } catch (JsonParseException e) {
                throw new InvalidInputException(
                        "El archivo no contiene JSON válido."
                );
            } catch (JsonMappingException e) {
                throw new InvalidInputException(
                        "El archivo contiene campos con formato incorrecto: "
                                + e.getOriginalMessage()
                );
            }

            log.info(
                    "Archivo leído. v{}",
                    snapshot.metadata().version()
            );

            snapshot = validateAndMigrate(snapshot);

            CategoryImportResult categoryImportResult =
                    categoryImportPort.importCategories(
                            snapshot.categories(),
                            loggedInUserId
                    );

            Map<UUID, UUID> categoryIdMap =
                    categoryImportResult.categoryIdMap();

            int totalImported =
                    categoryImportResult.importedCount();

            totalImported +=
                    transactionImportPort.importTransactions(
                            snapshot.transactions(),
                            loggedInUserId,
                            categoryIdMap
                    );

            totalImported +=
                    plannedTransactionImportPort.importPlannedTransactions(
                            snapshot.plannedTransactions(),
                            loggedInUserId,
                            categoryIdMap
                    );

            totalImported +=
                    investmentImportPort.importInvestments(
                            snapshot.investments(),
                            loggedInUserId
                    );

            totalImported +=
                    savingsGoalImportPort.importSavingsGoals(
                            snapshot.savingsGoals(),
                            loggedInUserId
                    );

            totalImported += importUserPreferences(
                    snapshot,
                    loggedInUserId
            );

            transactionImportVerificationPort
                    .verifyImportedTransactions(
                            loggedInUserId
                    );

            String summary = String.format(
                    "Importación exitosa. %d registros importados. v%s",
                    totalImported,
                    ExportVersion.CURRENT
            );

            log.info(summary);
            return summary;

        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Error al importar: {}",
                    e.getMessage(),
                    e
            );
            throw new InvalidInputException(
                    "Error inesperado al importar el archivo. "
                            + "Asegúrate de que sea compatible con v"
                            + ExportVersion.CURRENT
            );
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidInputException(
                    "El archivo es demasiado grande. "
                            + "El tamaño máximo permitido es 50 MB."
            );
        }
    }

    private UserDataSnapshot validateAndMigrate(
            UserDataSnapshot snapshot
    ) {
        ExportVersion fileVersion;
        try {
            fileVersion = ExportVersion.fromString(
                    snapshot.metadata().version()
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(
                    "El archivo no indica una versión válida: "
                            + snapshot.metadata().version()
            );
        }

        if (!fileVersion.isCompatibleWith(
                ExportVersion.CURRENT
        )) {
            throw new InvalidInputException(
                    String.format(
                            "Versión incompatible. Archivo v%s, sistema v%s.",
                            fileVersion,
                            ExportVersion.CURRENT
                    )
            );
        }

        if (fileVersion.needsMigration(
                ExportVersion.CURRENT
        )) {
            log.warn(
                    "Migrando de v{} a v{}",
                    fileVersion,
                    ExportVersion.CURRENT
            );
            return migrationChain.migrateToLatest(snapshot);
        }

        log.debug(
                "v{} compatible",
                fileVersion
        );
        return snapshot;
    }

    private int importUserPreferences(
            UserDataSnapshot snapshot,
            UUID loggedInUserId
    ) {
        if (snapshot.userPreferences() == null) {
            return 0;
        }

        userPreferencesImportPort.save(
                snapshot.userPreferences(),
                loggedInUserId
        );

        log.debug(
                "Preferencias de usuario importadas"
        );
        return 1;
    }
}