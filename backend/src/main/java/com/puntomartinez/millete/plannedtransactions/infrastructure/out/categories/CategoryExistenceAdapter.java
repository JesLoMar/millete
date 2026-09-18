package com.puntomartinez.millete.plannedtransactions.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryExistencePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CategoryExistenceAdapter
        implements CategoryExistencePort {

    private final CategoryRepository categoryRepository;

    public CategoryExistenceAdapter(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean existsForUser(
            UUID categoryId,
            UUID userId
    ) {
        return categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .isPresent();
    }
}