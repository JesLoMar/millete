package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;

public class Migration020to030 implements DataMigration {

    @Override
    public ExportVersion fromVersion() {
        return new ExportVersion(0, 2, 0);
    }

    @Override
    public ExportVersion toVersion() {
        return new ExportVersion(0, 3, 0);
    }

    @Override
    public String description() {
        return "Actualiza el contrato temporal a LocalDate e Instant";
    }

    @Override
    public UserDataSnapshot migrate(UserDataSnapshot snapshot) {
        UserDataSnapshot.SnapshotMetadata metadata =
                new UserDataSnapshot.SnapshotMetadata(
                        toVersion().toString(),
                        snapshot.metadata().exportDate(),
                        snapshot.metadata().appVersion()
                );

        return new UserDataSnapshot(
                metadata,
                snapshot.categories(),
                snapshot.transactions(),
                snapshot.plannedTransactions(),
                snapshot.investments(),
                snapshot.savingsGoals(),
                snapshot.userPreferences()
        );
    }
}
