package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MigrationChain")
class MigrationChainTest {

    @Test
    @DisplayName("Should migrate from v0.0.1 to current version")
    void shouldMigrateFrom001ToCurrent() {
        MigrationChain chain = new MigrationChain();

        UserDataSnapshot oldSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        "0.0.1", Instant.now(), "0.0.1"
                ),
                List.of(), List.of(), List.of(), List.of(), List.of(), null
        );

        UserDataSnapshot result = chain.migrateToLatest(oldSnapshot);

        assertThat(result).isNotNull();
        assertThat(result.metadata().version())
                .isEqualTo(ExportVersion.CURRENT.toString());
    }

    @Test
    @DisplayName("Should not migrate when already at current version")
    void shouldNotMigrateWhenAlreadyCurrent() {
        MigrationChain chain = new MigrationChain();

        UserDataSnapshot currentSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        ExportVersion.CURRENT.toString(),
                        Instant.now(),
                        ExportVersion.CURRENT.toString()
                ),
                List.of(), List.of(), List.of(), List.of(), List.of(), null
        );

        UserDataSnapshot result = chain.migrateToLatest(currentSnapshot);

        assertThat(result).isNotNull();
        assertThat(result.metadata().version())
                .isEqualTo(ExportVersion.CURRENT.toString());
    }

    @Test
    @DisplayName("Migration001to010 should have correct versions")
    void migration001to010ShouldHaveCorrectVersions() {
        Migration001to010 migration = new Migration001to010();

        assertThat(migration.fromVersion()).isEqualTo(new ExportVersion(0, 0, 1));
        assertThat(migration.toVersion()).isEqualTo(new ExportVersion(0, 1, 0));
        assertThat(migration.description()).isNotNull();
    }

    @Test
    @DisplayName("Migration001to010 should initialize userPreferences to null")
    void migration001to010ShouldInitializeUserPreferencesToNull() {
        Migration001to010 migration = new Migration001to010();

        UserDataSnapshot oldSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        "0.0.1", Instant.now(), "0.0.1"
                ),
                List.of(), List.of(), List.of(), List.of(), List.of(), null
        );

        UserDataSnapshot result = migration.migrate(oldSnapshot);

        assertThat(result.userPreferences()).isNull();
    }

    @Test
    @DisplayName("Migration010to020 should have correct versions")
    void migration010to020ShouldHaveCorrectVersions() {
        Migration010to020 migration = new Migration010to020();

        assertThat(migration.fromVersion()).isEqualTo(new ExportVersion(0, 1, 0));
        assertThat(migration.toVersion()).isEqualTo(new ExportVersion(0, 2, 0));
        assertThat(migration.description()).isNotNull();
    }

    @Test
    @DisplayName("Migration010to020 should normalize negative amounts in planned transactions")
    void migration010to020ShouldNormalizeNegativeAmounts() {
        Migration010to020 migration = new Migration010to020();

        PlannedTransactionSnapshot negativePtx = new PlannedTransactionSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("-100.00"), "EXPENSE", "Alquiler",
                "MONTHS", 1, LocalDate.now(), null,
                Instant.now(), Instant.now(), true, null
        );

        UserDataSnapshot oldSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        "0.1.0", Instant.now(), "0.1.0"
                ),
                List.of(), List.of(), List.of(negativePtx),
                List.of(), List.of(), null
        );

        UserDataSnapshot result = migration.migrate(oldSnapshot);

        assertThat(result.plannedTransactions()).hasSize(1);
        assertThat(result.plannedTransactions().getFirst().amount())
                .isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Migration010to020 should not modify positive amounts")
    void migration010to020ShouldNotModifyPositiveAmounts() {
        Migration010to020 migration = new Migration010to020();

        PlannedTransactionSnapshot positivePtx = new PlannedTransactionSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("200.00"), "EXPENSE", "Alquiler",
                "MONTHS", 1, LocalDate.now(), null,
                Instant.now(), Instant.now(), true, null
        );

        UserDataSnapshot oldSnapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        "0.1.0", Instant.now(), "0.1.0"
                ),
                List.of(), List.of(), List.of(positivePtx),
                List.of(), List.of(), null
        );

        UserDataSnapshot result = migration.migrate(oldSnapshot);

        assertThat(result.plannedTransactions().getFirst().amount())
                .isEqualByComparingTo("200.00");
    }
}