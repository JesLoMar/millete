package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MigrationChain - Cadena de migraciones")
class MigrationChainTest {

    @Test
    @DisplayName("Debe migrar de v0.0.1 a v0.1.0 correctamente")
    void migrateToLatest_shouldMigrateFrom001To010() {
        MigrationChain chain = new MigrationChain();

        UserDataSnapshot oldSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata("0.0.1", LocalDateTime.now(), "0.0.1"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, null, null
        );

        UserDataSnapshot result = chain.migrateToLatest(oldSnapshot);

        assertNotNull(result);
        assertEquals("0.1.0", result.metadata().version());
    }

    @Test
    @DisplayName("No debe migrar si ya está en versión actual")
    void migrateToLatest_shouldNotMigrate_whenAlreadyCurrent() {
        MigrationChain chain = new MigrationChain();

        UserDataSnapshot currentSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata("0.1.0", LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, null, null
        );

        UserDataSnapshot result = chain.migrateToLatest(currentSnapshot);

        assertNotNull(result);
        assertEquals("0.1.0", result.metadata().version());
    }

    @Test
    @DisplayName("Migration001to010 debe tener versiones correctas")
    void migration001to010_shouldHaveCorrectVersions() {
        Migration001to010 migration = new Migration001to010();

        assertEquals(new ExportVersion(0, 0, 1), migration.fromVersion());
        assertEquals(new ExportVersion(0, 1, 0), migration.toVersion());
        assertNotNull(migration.description());
    }

    @Test
    @DisplayName("Migration001to010 debe inicializar campos nuevos a null")
    void migration001to010_shouldInitializeNewFieldsToNull() {
        Migration001to010 migration = new Migration001to010();

        UserDataSnapshot oldSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata("0.0.1", LocalDateTime.now(), "0.0.1"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, null, null
        );

        UserDataSnapshot result = migration.migrate(oldSnapshot);

        assertNull(result.userPreferences());
        assertNull(result.goalUnits());
        assertNull(result.goalMembers());
        assertNull(result.goalContributions());
    }
}
