package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;
import java.util.UUID;

public record AddTransactionData(
        String description,
        BigDecimal amount,
        String categoryName,
        UUID categoryId
) implements InterpretationData {
}