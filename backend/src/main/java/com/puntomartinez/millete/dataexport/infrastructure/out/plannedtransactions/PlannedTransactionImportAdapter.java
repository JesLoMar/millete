package com.puntomartinez.millete.dataexport.infrastructure.out.plannedtransactions;

import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.PlannedTransactionImportPort;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PlannedTransactionImportAdapter
        implements PlannedTransactionImportPort {

    private final PlannedTransactionRepository plannedTransactionRepository;

    public PlannedTransactionImportAdapter(
            PlannedTransactionRepository plannedTransactionRepository
    ) {
        this.plannedTransactionRepository =
                plannedTransactionRepository;
    }

    @Override
    public int importPlannedTransactions(
            List<PlannedTransactionSnapshot> plannedTransactions,
            UUID userId,
            Map<UUID, UUID> categoryIdMap
    ) {
        if (plannedTransactions == null
                || plannedTransactions.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (PlannedTransactionSnapshot plannedTransaction :
                plannedTransactions) {
            if (!plannedTransaction.active()) {
                continue;
            }

            UUID newCategoryId = null;
            if (plannedTransaction.categoryId() != null) {
                newCategoryId =
                        categoryIdMap.get(
                                plannedTransaction.categoryId()
                        );
            }

            PlannedTransaction importedPlannedTransaction =
                    PlannedTransaction.reconstitute(
                            UUID.randomUUID(),
                            userId,
                            newCategoryId,
                            plannedTransaction.amount(),
                            Transaction.TransactionType.valueOf(
                                    plannedTransaction.type()
                            ),
                            plannedTransaction.description(),
                            PlannedTransaction.FrequencyType.valueOf(
                                    plannedTransaction.frequencyType()
                            ),
                            plannedTransaction.frequencyInterval(),
                            plannedTransaction.startDate(),
                            plannedTransaction.endDate(),
                            plannedTransaction.createdAt(),
                            plannedTransaction.modifiedAt(),
                            plannedTransaction.active(),
                            plannedTransaction.lastExecutedDate(),
                            0
                    );

            plannedTransactionRepository.save(
                    importedPlannedTransaction
            );
            count++;
        }

        return count;
    }
}