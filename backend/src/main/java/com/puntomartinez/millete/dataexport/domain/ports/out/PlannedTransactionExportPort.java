package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;

import java.util.List;
import java.util.UUID;

public interface PlannedTransactionExportPort {

    List<PlannedTransactionSnapshot> findAllByUserId(UUID userId);
}