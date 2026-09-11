package com.puntomartinez.millete.categories.domain.ports.in;

import java.math.BigDecimal;

public record UpdateCategoryCommand(
        String name,
        String color,
        BigDecimal budgetLimit
) {}
