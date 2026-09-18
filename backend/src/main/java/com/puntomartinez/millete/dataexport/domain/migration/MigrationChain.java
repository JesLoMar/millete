package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Cadena de migraciones de snapshots de datos.
 *
 * <p>Las migraciones son clases stateless instanciadas manualmente
 * (no son Spring beans). Esto es aceptable porque no necesitan
 * dependencias inyectadas. Si se añaden migraciones que necesiten
 * dependencias (por ejemplo, un repositorio), convertirlas en beans
 * e inyectarlas en esta clase.</p>
 *
 * <p>CONTRATO DE ExportVersion: Al añadir una nueva migración,
 * ACTUALIZAR {@code ExportVersion.CURRENT} al {@code toVersion()}
 * de la nueva migración. {@link #validateChain()} detecta al
 * arrancar si hay inconsistencia.</p>
 */
@Slf4j
@Component
public class MigrationChain {

    private final List<DataMigration> migrations;

    public MigrationChain() {
        this.migrations = new ArrayList<>();
        registerMigrations();
        migrations.sort(
                Comparator.comparing(DataMigration::fromVersion)
        );
        validateChain();
    }

    private void registerMigrations() {
        migrations.add(new Migration001to010());
        migrations.add(new Migration010to020());
    }

    public UserDataSnapshot migrateToLatest(
            UserDataSnapshot snapshot
    ) {
        ExportVersion currentVersion =
                ExportVersion.fromString(
                        snapshot.metadata().version()
                );

        UserDataSnapshot result = snapshot;

        if (migrations.isEmpty()) {
            if (!currentVersion.equals(ExportVersion.CURRENT)) {
                log.warn(
                        "Aviso: archivo v{} pero sistema en v{}",
                        currentVersion,
                        ExportVersion.CURRENT
                );
            }
            return result;
        }

        log.info("Migrando desde v{}", currentVersion);

        for (DataMigration migration : migrations) {
            if (currentVersion.compareTo(
                    migration.fromVersion()
            ) >= 0
                    && currentVersion.compareTo(
                    migration.toVersion()
            ) < 0) {

                log.info("  → {}", migration.description());

                try {
                    result = migration.migrate(result);
                    currentVersion = migration.toVersion();
                    log.info("  ✓ v{}", currentVersion);
                } catch (Exception e) {
                    throw new InvalidInputException(
                            "Error en migración "
                                    + migration.fromVersion()
                                    + " → "
                                    + migration.toVersion(),
                            e
                    );
                }
            }
        }

        log.info("Migración completada. v{}", currentVersion);
        return result;
    }

    private void validateChain() {
        if (migrations.isEmpty()) {
            log.info(
                    "Sin migraciones. Versión actual: v{}",
                    ExportVersion.CURRENT
            );
            return;
        }

        for (int i = 0; i < migrations.size() - 1; i++) {
            ExportVersion currentTo =
                    migrations.get(i).toVersion();
            ExportVersion nextFrom =
                    migrations.get(i + 1).fromVersion();

            if (!currentTo.equals(nextFrom)) {
                throw new IllegalStateException(
                        "Cadena de migraciones rota: migración "
                                + i
                                + " termina en v"
                                + currentTo
                                + " pero la siguiente empieza en v"
                                + nextFrom
                );
            }
        }

        ExportVersion lastTo =
                migrations.get(migrations.size() - 1).toVersion();

        if (!lastTo.equals(ExportVersion.CURRENT)) {
            throw new IllegalStateException(
                    "Inconsistencia de versión: la última migración "
                            + "llega a v" + lastTo
                            + " pero ExportVersion.CURRENT es v"
                            + ExportVersion.CURRENT
                            + ". Actualiza ExportVersion.CURRENT."
            );
        }

        log.info(
                "Cadena de migraciones validada. {} migraciones. v{}",
                migrations.size(),
                ExportVersion.CURRENT
        );
    }
}