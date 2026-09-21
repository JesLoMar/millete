package com.puntomartinez.millete.dataexport.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.model.CategorySnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryExportAdapter")
class CategoryExportAdapterTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryExportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("findByUserId should map categories to snapshots")
    void findByUserIdShouldMapCategoriesToSnapshots() {
        Category cat = Category.reconstitute(
                UUID.randomUUID(), userId, "Food", "#FF5733",
                new BigDecimal("500.00"),
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        when(categoryRepository.findByUserId(userId)).thenReturn(List.of(cat));

        List<CategorySnapshot> result = adapter.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Food");
        assertThat(result.getFirst().color()).isEqualTo("#FF5733");
        assertThat(result.getFirst().budgetLimit()).isEqualByComparingTo("500.00");
        assertThat(result.getFirst().active()).isTrue();
    }

    @Test
    @DisplayName("findByUserId should return empty list when no categories")
    void findByUserIdShouldReturnEmptyListWhenNoCategories() {
        when(categoryRepository.findByUserId(userId)).thenReturn(List.of());

        List<CategorySnapshot> result = adapter.findByUserId(userId);

        assertThat(result).isEmpty();
    }
}