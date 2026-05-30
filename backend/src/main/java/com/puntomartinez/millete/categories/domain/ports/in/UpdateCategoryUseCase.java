package com.puntomartinez.millete.categories.domain.ports.in;

import com.puntomartinez.millete.categories.domain.model.Category;
import java.util.UUID;

public interface UpdateCategoryUseCase {
    Category update(UUID id, UUID userId, UpdateCategoryCommand command);
}