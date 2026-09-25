package com.puntomartinez.millete.dataexport.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.model.CategoryImportResult;
import com.puntomartinez.millete.dataexport.domain.model.CategorySnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryImportAdapter")
class CategoryImportAdapterTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryImportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("importCategories should create new categories")
    void importCategoriesShouldCreateNewCategories() {
        UUID sourceCategoryId = UUID.randomUUID();
        CategorySnapshot snapshot = new CategorySnapshot(
                sourceCategoryId, UUID.randomUUID(), "Food", "#FF5733",
                new BigDecimal("500.00"),
                Instant.now(), Instant.now(), true
        );

        when(categoryRepository.findByUserId(userId)).thenReturn(new ArrayList<>());
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CategoryImportResult result = adapter.importCategories(
                List.of(snapshot), userId
        );

        assertThat(result.importedCount()).isEqualTo(1);
        assertThat(result.categoryIdMap()).containsKey(sourceCategoryId);
        assertThat(result.categoryIdMap().get(sourceCategoryId))
                .isNotEqualTo(sourceCategoryId);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("importCategories should reuse existing category when name matches")
    void importCategoriesShouldReuseExistingCategory() {
        UUID existingCategoryId = UUID.randomUUID();
        UUID sourceCategoryId = UUID.randomUUID();

        Category existingCategory = Category.reconstitute(
                existingCategoryId, userId, "Food", "#00FF00",
                new BigDecimal("300.00"),
                Instant.now(), Instant.now(), true
        );

        CategorySnapshot snapshot = new CategorySnapshot(
                sourceCategoryId, UUID.randomUUID(), "Food", "#FF5733",
                new BigDecimal("500.00"),
                Instant.now(), Instant.now(), true
        );

        when(categoryRepository.findByUserId(userId))
                .thenReturn(List.of(existingCategory));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CategoryImportResult result = adapter.importCategories(
                List.of(snapshot), userId
        );

        assertThat(result.categoryIdMap().get(sourceCategoryId))
                .isEqualTo(existingCategoryId);
    }

    @Test
    @DisplayName("importCategories should skip inactive categories")
    void importCategoriesShouldSkipInactiveCategories() {
        CategorySnapshot inactiveSnapshot = new CategorySnapshot(
                UUID.randomUUID(), UUID.randomUUID(), "Inactive", "#FF5733",
                new BigDecimal("500.00"),
                Instant.now(), Instant.now(), false
        );

        CategoryImportResult result = adapter.importCategories(
                List.of(inactiveSnapshot), userId
        );

        assertThat(result.importedCount()).isZero();
        verify(categoryRepository, never()).save(any());
    }
}