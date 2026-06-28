package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;

/**
 * Migración de formato de exportación v0.0.1 → v0.1.0.
 * Añade soporte para:
 *  - UserPreferences
 *  - GoalUnit (metas grupales)
 *  - GoalMember (miembros de metas grupales)
 *  - GoalContribution (aportaciones a metas grupales)
 *
 * Los snapshots v0.0.1 no contienen estos campos, por lo que la migración
 * los inicializa a null/listas vacías para mantener compatibilidad.
 */
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
        // Los campos nuevos (userPreferences, goalUnits, goalMembers, goalContributions)
        // ya son manejados por @JsonIgnoreProperties(ignoreUnknown = true) en el
        // deserializador, por lo que al leer un snapshot v0.0.1 estos campos
        // simplemente serán null o listas vacías.
        //
        // Esta migración explícita reconstruye el snapshot con los campos
        // inicializados para claridad y consistencia, y actualiza la versión.
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
                null,                       // userPreferences
                null,                       // goalUnits
                null,                       // goalMembers
                null                        // goalContributions
        );
    }
}
