package com.puntomartinez.millete.transactions.domain.ports.in;

import java.util.UUID;

public interface UnassignCategoryFromTransactionsUseCase {

    void unassignCategory(UUID categoryId, UUID userId);
}