package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class MigrationChain {

    private final List<DataMigration> migrations;

    public MigrationChain() {
        this.migrations = new ArrayList<>();
        registerMigrations();
        migrations.sort(Comparator.comparing(DataMigration::fromVersion));
        validateChain();
    }

    private void registerMigrations() {
        // Sin migraciones - primera versión del formato (0.0.1)
        // Añadir aquí las migraciones cuando el esquema evolucione
    }

    public UserDataSnapshot migrateToLatest(UserDataSnapshot snapshot) {
        ExportVersion currentVersion = ExportVersion.fromString(snapshot.metadata().version());
        UserDataSnapshot result = snapshot;

        // Si no hay migraciones, verificar que la versión coincide
        if (migrations.isEmpty()) {
            if (!currentVersion.equals(ExportVersion.CURRENT)) {
                System.out.println("Aviso: archivo v" + currentVersion
                        + " pero sistema en v" + ExportVersion.CURRENT);
            }
            return result;
        }

        System.out.println("Migrando desde v" + currentVersion);

        for (DataMigration migration : migrations) {
            // Solo aplicar migraciones posteriores a la versión del archivo
            if (currentVersion.compareTo(migration.fromVersion()) >= 0
                    && currentVersion.compareTo(migration.toVersion()) < 0) {

                System.out.println("  → " + migration.description());

                try {
                    result = migration.migrate(result);
                    currentVersion = migration.toVersion();
                    System.out.println("  ✓ v" + currentVersion);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Error en migración " + migration.fromVersion()
                                    + " → " + migration.toVersion() + ": " + e.getMessage(), e);
                }
            }
        }

        System.out.println("Migración completada. v" + currentVersion);
        return result;
    }

    private void validateChain() {
        if (migrations.isEmpty()) {
            System.out.println("Sin migraciones. Versión actual: v" + ExportVersion.CURRENT);
            return;
        }

        // Verificar que no hay huecos entre migraciones
        for (int i = 0; i < migrations.size() - 1; i++) {
            ExportVersion currentTo = migrations.get(i).toVersion();
            ExportVersion nextFrom = migrations.get(i + 1).fromVersion();

            if (!currentTo.equals(nextFrom)) {
                throw new IllegalStateException(
                        "Cadena rota: migración " + i + " termina en v" + currentTo
                                + " pero la siguiente empieza en v" + nextFrom);
            }
        }

        // Verificar que la última migración llega a la versión actual
        ExportVersion lastTo = migrations.get(migrations.size() - 1).toVersion();
        if (!lastTo.equals(ExportVersion.CURRENT)) {
            throw new IllegalStateException(
                    "Última migración llega a v" + lastTo
                            + " pero la versión actual es v" + ExportVersion.CURRENT);
        }
    }
}