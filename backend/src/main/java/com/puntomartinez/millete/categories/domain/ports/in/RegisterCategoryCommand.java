package com.puntomartinez.millete.categories.domain.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterCategoryCommand(
        UUID userId,
        String name,
        String description,
        String color,
        BigDecimal budgetLimit
) {
}