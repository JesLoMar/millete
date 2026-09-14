package com.puntomartinez.millete.dataexport.domain.ports.out;

import java.util.UUID;

public interface TransactionImportVerificationPort {

    void verifyImportedTransactions(UUID userId);
}