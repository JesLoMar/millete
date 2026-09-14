package com.puntomartinez.millete.dataexport.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.model.CategorySnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.CategoryExportPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CategoryExportAdapter implements CategoryExportPort {

    private final CategoryRepository categoryRepository;

    public CategoryExportAdapter(
            CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategorySnapshot> findByUserId(UUID userId) {
        return categoryRepository.findByUserId(userId)
                .stream()
                .map(category ->
                        new CategorySnapshot(
                                category.getId(),
                                category.getUserId(),
                                category.getName(),
                                category.getColor(),
                                category.getBudgetLimit(),
                                category.getCreatedAt(),
                                category.getModifiedAt(),
                                category.isActive()
                        )
                )
                .toList();
    }
}