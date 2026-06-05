package com.puntomartinez.millete.categories.domain.ports.in;

import com.puntomartinez.millete.categories.domain.model.Category;
import java.util.List;
import java.util.UUID;

public interface GetCategoryUseCase {
    List<Category> findByUserId(UUID userId);
}