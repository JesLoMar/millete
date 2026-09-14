package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlannedTransactionImportPort {

    int importPlannedTransactions(
            List<PlannedTransactionSnapshot> plannedTransactions,
            UUID userId,
            Map<UUID, UUID> categoryIdMap
    );
}