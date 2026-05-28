package com.puntomartinez.millete.categories.domain.ports.in;

import com.puntomartinez.millete.categories.domain.model.Category;
import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateCategoryUseCase {
    Category update(UUID id, UpdateCategoryCommand command);

    record UpdateCategoryCommand(
            String nombre,
            String color,
            BigDecimal budgetLimit
    ) {}
}