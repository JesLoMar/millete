package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionExportPort {

    List<TransactionSnapshot> findAllByUserId(UUID userId);

    List<TransactionSnapshot> findByUserIdAndDateBetween(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    );
}