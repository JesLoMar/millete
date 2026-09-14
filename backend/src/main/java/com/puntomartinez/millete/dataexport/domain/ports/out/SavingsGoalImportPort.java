package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.SavingsGoalSnapshot;

import java.util.List;
import java.util.UUID;

public interface SavingsGoalImportPort {

    int importSavingsGoals(
            List<SavingsGoalSnapshot> savingsGoals,
            UUID userId
    );
}