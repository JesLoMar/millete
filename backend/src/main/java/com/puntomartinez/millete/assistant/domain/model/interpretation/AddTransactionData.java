package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;

public record AddTransactionData(
        String description,
        BigDecimal amount,
        String categoryName
) implements InterpretationData {
}