package com.puntomartinez.millete.assistant.domain.model.interpretation;

import java.math.BigDecimal;

public record AddCategoryData(
        String name,
        String description,
        BigDecimal budgetLimit
) implements InterpretationData {
}