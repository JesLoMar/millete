package com.puntomartinez.millete.dashboard.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CategoryQueryPostgresAdapter implements CategoryQueryPort {

    private final CategoryRepository categoryRepository;

    public CategoryQueryPostgresAdapter(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryData> findByUserId(UUID userId) {
        return categoryRepository.findByUserId(userId)
                .stream()
                .map(this::toCategoryData)
                .toList();
    }

    @Override
    public List<CategoryData> findByIdsAndUserId(
            UUID userId,
            List<UUID> categoryIds
    ) {
        return categoryRepository.findByIdsAndUserId(
                        userId,
                        categoryIds
                )
                .stream()
                .map(this::toCategoryData)
                .toList();
    }

    @Override
    public List<CategoryData> findCategoriesWithBudgetByUserId(UUID userId) {
        return categoryRepository.findCategoriesWithBudgetByUserId(userId)
                .stream()
                .map(this::toCategoryData)
                .toList();
    }

    private CategoryData toCategoryData(Category category) {
        return new CategoryData(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getBudgetLimit()
        );
    }
}