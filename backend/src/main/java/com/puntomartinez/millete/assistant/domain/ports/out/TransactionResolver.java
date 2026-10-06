package com.puntomartinez.millete.assistant.domain.ports.out;

import com.puntomartinez.millete.assistant.domain.model.interpretation.EditTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.TransactionResolution;

import java.util.UUID;

public interface TransactionResolver {

    TransactionResolution resolve(
            UUID userId,
            EditTransactionData.TransactionTarget target
    );
}