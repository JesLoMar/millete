package com.puntomartinez.millete.dataexport.infrastructure.out.transactions;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.ports.out.TransactionImportVerificationPort;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class TransactionImportVerificationAdapter
        implements TransactionImportVerificationPort {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionImportVerificationAdapter(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository) {

        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void verifyImportedTransactions(
            UUID userId) {

        var allTransactions =
                transactionRepository.findAllByUserId(
                        userId
                );

        var categoryIds = allTransactions.stream()
                .map(Transaction::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        if (categoryIds.isEmpty()) {
            log.debug(
                    "No hay transacciones con categoría para verificar"
            );
            return;
        }

        var resolvableCategoryIds =
                categoryRepository.findByIdsAndUserId(
                                userId,
                                categoryIds.stream().toList()
                        )
                        .stream()
                        .map(Category::getId)
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        int orphanCount = 0;

        for (Transaction transaction :
                allTransactions) {

            UUID categoryId =
                    transaction.getCategoryId();

            if (categoryId == null) {
                continue;
            }

            if (!resolvableCategoryIds.contains(categoryId)) {
                orphanCount++;

                log.warn(
                        "Transacción {} tiene categoría {} no resoluble para el usuario {}",
                        transaction.getId(),
                        categoryId,
                        userId
                );
            }
        }

        if (orphanCount > 0) {
            log.warn(
                    "{} transacciones tienen categorías no resueltas tras la importación",
                    orphanCount
            );
        } else {
            log.debug(
                    "Todas las transacciones tienen categorías resolubles correctamente"
            );
        }
    }
}