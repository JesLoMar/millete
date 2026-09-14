package com.puntomartinez.millete.dataexport.infrastructure.out.transactions;

import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.TransactionImportPort;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TransactionImportAdapter implements TransactionImportPort {

    private final TransactionRepository transactionRepository;

    public TransactionImportAdapter(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public int importTransactions(
            List<TransactionSnapshot> transactions,
            UUID userId,
            Map<UUID, UUID> categoryIdMap
    ) {

        if (transactions == null || transactions.isEmpty()) {
            return 0;
        }

        int importedCount = 0;

        for (TransactionSnapshot snapshot : transactions) {

            if (!snapshot.active()) {
                continue;
            }

            UUID categoryId = snapshot.categoryId();

            if (categoryId != null) {
                categoryId = categoryIdMap.get(categoryId);
            }

            Transaction transaction =
                    Transaction.reconstitute(
                            UUID.randomUUID(),
                            userId,
                            categoryId,
                            snapshot.amount(),
                            snapshot.date(),
                            Transaction.TransactionType.valueOf(
                                    snapshot.type()
                            ),
                            snapshot.description(),
                            snapshot.createdAt(),
                            snapshot.modifiedAt(),
                            snapshot.active()
                    );

            transactionRepository.save(transaction);
            importedCount++;
        }

        return importedCount;
    }
}