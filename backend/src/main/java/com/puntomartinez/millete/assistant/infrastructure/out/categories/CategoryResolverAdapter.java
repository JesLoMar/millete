package com.puntomartinez.millete.assistant.infrastructure.out.categories;

import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryCandidate;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolution;
import com.puntomartinez.millete.assistant.domain.ports.out.CategoryResolver;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.adapters.VerdictCategorySelector;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public final class CategoryResolverAdapter
        implements CategoryResolver {

    private final CategoryRepository categoryRepository;
    private final VerdictCategorySelector verdictCategorySelector;

    public CategoryResolverAdapter(
            CategoryRepository categoryRepository,
            VerdictCategorySelector verdictCategorySelector
    ) {
        this.categoryRepository =
                Objects.requireNonNull(
                        categoryRepository,
                        "categoryRepository cannot be null"
                );

        this.verdictCategorySelector =
                Objects.requireNonNull(
                        verdictCategorySelector,
                        "verdictCategorySelector cannot be null"
                );
    }

    @Override
    public CategoryResolution resolve(
            UUID userId,
            String categoryReference
    ) {
        Objects.requireNonNull(
                userId,
                "userId cannot be null"
        );

        Objects.requireNonNull(
                categoryReference,
                "categoryReference cannot be null"
        );

        if (categoryReference.isBlank()) {
            throw new IllegalArgumentException(
                    "categoryReference cannot be blank"
            );
        }

        var categories =
                categoryRepository.findByUserId(
                        userId
                );

        if (categories.isEmpty()) {
            return CategoryResolution.notFound(
                    categoryReference,
                    0.0,
                    0.0
            );
        }

        var candidates =
                categories.stream()
                        .map(category ->
                                new CategoryCandidate(
                                        category.getId(),
                                        category.getName()
                                )
                        )
                        .toList();

        return verdictCategorySelector.select(
                categoryReference.trim(),
                candidates
        );
    }
}