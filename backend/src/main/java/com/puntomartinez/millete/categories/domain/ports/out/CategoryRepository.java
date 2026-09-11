package com.puntomartinez.millete.categories.domain.ports.out;

import com.puntomartinez.millete.categories.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    List<Category> findByUserId(UUID userId);

    List<Category> findCategoriesWithBudgetByUserId(UUID userId);

    List<Category> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search
    );

    long countByUserIdAndFilters(
            UUID userId,
            String search
    );
}