package com.puntomartinez.millete.dataexport.domain.migration;

import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;

import java.math.BigDecimal;
import java.util.List;

public class Migration010to020 implements DataMigration {

    @Override
    public ExportVersion fromVersion() {
        return new ExportVersion(0, 1, 0);
    }

    @Override
    public ExportVersion toVersion() {
        return new ExportVersion(0, 2, 0);
    }

    @Override
    public String description() {
        return "Normaliza los importes de las transacciones planificadas a valores positivos";
    }

    @Override
    public UserDataSnapshot migrate(UserDataSnapshot snapshot) {

        List<PlannedTransactionSnapshot> migratedPlannedTransactions =
                snapshot.plannedTransactions() == null
                        ? null
                        : snapshot.plannedTransactions()
                        .stream()
                        .map(this::migratePlannedTransaction)
                        .toList();

        UserDataSnapshot.SnapshotMetadata updatedMetadata =
                new UserDataSnapshot.SnapshotMetadata(
                        toVersion().toString(),
                        snapshot.metadata().exportDate(),
                        snapshot.metadata().appVersion()
                );

        return new UserDataSnapshot(
                updatedMetadata,
                snapshot.categories(),
                snapshot.transactions(),
                migratedPlannedTransactions,
                snapshot.investments(),
                snapshot.savingsGoals(),
                snapshot.userPreferences()
        );
    }

    private PlannedTransactionSnapshot migratePlannedTransaction(
            PlannedTransactionSnapshot plannedTransaction
    ) {
        BigDecimal amount = plannedTransaction.amount();

        if (amount == null || amount.signum() >= 0) {
            return plannedTransaction;
        }

        return new PlannedTransactionSnapshot(
                plannedTransaction.id(),
                plannedTransaction.userId(),
                plannedTransaction.categoryId(),
                amount.abs(),
                plannedTransaction.type(),
                plannedTransaction.description(),
                plannedTransaction.frequencyType(),
                plannedTransaction.frequencyInterval(),
                plannedTransaction.startDate(),
                plannedTransaction.endDate(),
                plannedTransaction.createdAt(),
                plannedTransaction.modifiedAt(),
                plannedTransaction.active(),
                plannedTransaction.lastExecutedDate()
        );
    }
}