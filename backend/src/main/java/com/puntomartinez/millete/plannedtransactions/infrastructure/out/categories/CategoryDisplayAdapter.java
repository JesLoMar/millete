package com.puntomartinez.millete.plannedtransactions.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryDisplayPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CategoryDisplayAdapter
        implements CategoryDisplayPort {

    private final CategoryRepository categoryRepository;

    public CategoryDisplayAdapter(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Optional<CategoryDisplay> findByIdAndUserId(
            UUID categoryId,
            UUID userId
    ) {
        return categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .map(this::toDisplay);
    }

    @Override
    public List<CategoryDisplay> findByUserId(UUID userId) {
        return categoryRepository
                .findByUserId(userId)
                .stream()
                .map(this::toDisplay)
                .toList();
    }

    private CategoryDisplay toDisplay(Category category) {
        return new CategoryDisplay(
                category.getId(),
                category.getName(),
                category.getColor()
        );
    }
}