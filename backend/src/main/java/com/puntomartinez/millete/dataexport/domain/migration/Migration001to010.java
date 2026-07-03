package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;

public class Migration001to010 implements DataMigration {

    @Override
    public ExportVersion fromVersion() {
        return new ExportVersion(0, 0, 1);
    }

    @Override
    public ExportVersion toVersion() {
        return new ExportVersion(0, 1, 0);
    }

    @Override
    public String description() {
        return "Añade soporte para UserPreferences, GoalUnit, GoalMember y GoalContribution";
    }

    @Override
    public UserDataSnapshot migrate(UserDataSnapshot snapshot) {


        UserDataSnapshot.SnapshotMetadata updatedMetadata = new UserDataSnapshot.SnapshotMetadata(
                toVersion().toString(),
                snapshot.metadata().exportDate(),
                snapshot.metadata().appVersion()
        );
        return new UserDataSnapshot(
                updatedMetadata,
                snapshot.categories(),
                snapshot.transactions(),
                snapshot.plannedTransactions(),
                snapshot.investments(),
                snapshot.savingsGoals(),
                null,
                null,
                null,
                null
        );
    }
}
