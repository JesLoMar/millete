package com.puntomartinez.millete.dataexport.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.model.CategoryImportResult;
import com.puntomartinez.millete.dataexport.domain.model.CategorySnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.CategoryImportPort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CategoryImportAdapter implements CategoryImportPort {

    private final CategoryRepository categoryRepository;

    public CategoryImportAdapter(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryImportResult importCategories(
            List<CategorySnapshot> categories,
            UUID userId
    ) {

        Map<UUID, UUID> categoryIdMap =
                new HashMap<>();

        if (categories == null || categories.isEmpty()) {
            return new CategoryImportResult(
                    categoryIdMap,
                    0
            );
        }

        Map<String, Category> existingByName =
                new HashMap<>();

        for (Category existing :
                categoryRepository.findByUserId(userId)) {

            if (existing.isActive()) {
                existingByName.put(
                        existing.getName().toLowerCase(),
                        existing
                );
            }
        }

        int importedCount = 0;

        for (CategorySnapshot category : categories) {

            if (!category.active()) {
                continue;
            }

            String nameLower =
                    category.name().toLowerCase();

            Category existing =
                    existingByName.get(nameLower);

            if (existing != null) {

                existing.updateDetails(
                        category.name(),
                        category.color(),
                        category.budgetLimit()
                );

                categoryRepository.save(existing);

                categoryIdMap.put(
                        category.id(),
                        existing.getId()
                );

            } else {

                UUID newId = UUID.randomUUID();

                Category importedCategory =
                        Category.reconstitute(
                                newId,
                                userId,
                                category.name(),
                                category.color(),
                                category.budgetLimit(),
                                category.createdAt(),
                                category.modifiedAt(),
                                category.active()
                        );

                categoryRepository.save(
                        importedCategory
                );

                categoryIdMap.put(
                        category.id(),
                        newId
                );

                existingByName.put(
                        nameLower,
                        importedCategory
                );

                importedCount++;
            }
        }

        return new CategoryImportResult(
                categoryIdMap,
                importedCount
        );
    }
}