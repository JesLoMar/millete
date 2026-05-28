package com.puntomartinez.millete.categories.domain.ports.in;

import com.puntomartinez.millete.categories.domain.model.Category;

import java.math.BigDecimal;
import java.util.UUID;

public interface RegisterCategoryUseCase {
    Category register(RegisterCategoryCommand command);

    record RegisterCategoryCommand(
            UUID userId,
            String nombre,
            String color,
            BigDecimal budgetLimit
    ) {}
}