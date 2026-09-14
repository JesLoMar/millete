package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;

import java.util.List;
import java.util.UUID;

public interface TransactionImportPort {

    int importTransactions(
            List<TransactionSnapshot> transactions,
            UUID userId,
            java.util.Map<UUID, UUID> categoryIdMap
    );
}