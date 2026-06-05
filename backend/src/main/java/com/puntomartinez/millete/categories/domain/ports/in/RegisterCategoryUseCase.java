package com.puntomartinez.millete.categories.domain.ports.in;

import com.puntomartinez.millete.categories.domain.model.Category;

public interface RegisterCategoryUseCase {
    Category register(RegisterCategoryCommand command);
}