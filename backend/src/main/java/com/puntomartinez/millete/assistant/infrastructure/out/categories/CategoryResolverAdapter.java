package com.puntomartinez.millete.assistant.infrastructure.out.categories;

import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryCandidate;
import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolution;
import com.puntomartinez.millete.assistant.domain.ports.out.CategoryResolver;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.adapters.VerdictCategorySelector;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
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

        var reference =
                categoryReference.trim();

        var categories =
                categoryRepository.findByUserId(
                        userId
                );

        if (categories.isEmpty()) {
            return CategoryResolution.notFound(
                    reference,
                    0.0,
                    0.0
            );
        }

        var exactMatches =
                categories.stream()
                        .filter(category ->
                                normalize(category.getName())
                                        .equals(
                                                normalize(reference)
                                        )
                        )
                        .toList();

        if (exactMatches.size() == 1) {
            var category =
                    exactMatches.getFirst();

            return CategoryResolution.found(
                    reference,
                    new CategoryCandidate(
                            category.getId(),
                            category.getName()
                    ),
                    1.0,
                    1.0
            );
        }

        if (exactMatches.size() > 1) {
            return CategoryResolution.ambiguous(
                    reference,
                    1.0,
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
                reference,
                candidates
        );
    }

    private static String normalize(
            String value
    ) {
        var normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFD
                );

        return normalized
                .replaceAll(
                        "\\p{M}",
                        ""
                )
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}