package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.SavingsGoalSnapshot;

import java.util.List;
import java.util.UUID;

public interface SavingsGoalExportPort {

    List<SavingsGoalSnapshot> findAllByUserId(UUID userId);
}