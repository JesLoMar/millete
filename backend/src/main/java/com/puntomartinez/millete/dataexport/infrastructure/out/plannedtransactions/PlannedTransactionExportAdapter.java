package com.puntomartinez.millete.dataexport.infrastructure.out.plannedtransactions;

import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.PlannedTransactionExportPort;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PlannedTransactionExportAdapter
        implements PlannedTransactionExportPort {

    private final PlannedTransactionRepository plannedTransactionRepository;

    public PlannedTransactionExportAdapter(
            PlannedTransactionRepository plannedTransactionRepository
    ) {
        this.plannedTransactionRepository = plannedTransactionRepository;
    }

    @Override
    public List<PlannedTransactionSnapshot> findAllByUserId(UUID userId) {
        return plannedTransactionRepository.findAllByUserId(userId)
                .stream()
                .map(plannedTransaction ->
                        new PlannedTransactionSnapshot(
                                plannedTransaction.getId(),
                                plannedTransaction.getUserId(),
                                plannedTransaction.getCategoryId(),
                                plannedTransaction.getAmount(),
                                plannedTransaction.getType().name(),
                                plannedTransaction.getDescription(),
                                plannedTransaction.getFrequencyType().name(),
                                plannedTransaction.getFrequencyInterval(),
                                plannedTransaction.getStartDate(),
                                plannedTransaction.getEndDate(),
                                plannedTransaction.getCreatedAt(),
                                plannedTransaction.getModifiedAt(),
                                plannedTransaction.isActive(),
                                plannedTransaction.getLastExecutedDate()
                        )
                )
                .toList();
    }
}