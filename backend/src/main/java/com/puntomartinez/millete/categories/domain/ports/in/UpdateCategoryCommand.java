package com.puntomartinez.millete.categories.domain.ports.in;

import java.math.BigDecimal;

public record UpdateCategoryCommand(
        String nombre,
        String color,
        BigDecimal budgetLimit
) {}