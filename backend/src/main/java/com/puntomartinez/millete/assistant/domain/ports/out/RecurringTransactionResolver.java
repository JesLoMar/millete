package com.puntomartinez.millete.assistant.domain.ports.out;

import com.puntomartinez.millete.assistant.domain.model.interpretation.EditRecurringTransactionData;
import com.puntomartinez.millete.assistant.domain.model.interpretation.RecurringTransactionResolution;

import java.util.UUID;

public interface RecurringTransactionResolver {

    RecurringTransactionResolution resolve(
            UUID userId,
            EditRecurringTransactionData.RecurringTransactionTarget target,
            UUID categoryId
    );
}