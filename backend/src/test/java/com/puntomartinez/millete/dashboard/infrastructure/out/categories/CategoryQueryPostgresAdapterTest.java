package com.puntomartinez.millete.dashboard.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dashboard.domain.ports.out.CategoryQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryQueryPostgresAdapter")
class CategoryQueryPostgresAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryQueryPostgresAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CategoryQueryPostgresAdapter(categoryRepository);
    }

    @Test
    @DisplayName("Should map categories to CategoryData on findByUserId")
    void shouldMapCategoriesOnFindByUserId() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.reconstitute(
                categoryId, USER_ID, "Food", "#FF0000",
                new BigDecimal("100.00"),
                Instant.now(), Instant.now(), true
        );

        when(categoryRepository.findByUserId(USER_ID))
                .thenReturn(List.of(category));

        List<CategoryQueryPort.CategoryData> result =
                adapter.findByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(categoryId);
        assertThat(result.get(0).name()).isEqualTo("Food");
        assertThat(result.get(0).color()).isEqualTo("#FF0000");
        assertThat(result.get(0).budgetLimit())
                .isEqualByComparingTo("100.00");
        verify(categoryRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Should map categories on findByIdsAndUserId")
    void shouldMapCategoriesOnFindByIdsAndUserId() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.reconstitute(
                categoryId, USER_ID, "Food", "#FF0000",
                null,
                Instant.now(), Instant.now(), true
        );

        when(categoryRepository.findByIdsAndUserId(USER_ID, List.of(categoryId)))
                .thenReturn(List.of(category));

        List<CategoryQueryPort.CategoryData> result =
                adapter.findByIdsAndUserId(USER_ID, List.of(categoryId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("Should map categories on findCategoriesWithBudgetByUserId")
    void shouldMapCategoriesOnFindWithBudget() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.reconstitute(
                categoryId, USER_ID, "Food", "#FF0000",
                new BigDecimal("200.00"),
                Instant.now(), Instant.now(), true
        );

        when(categoryRepository.findCategoriesWithBudgetByUserId(USER_ID))
                .thenReturn(List.of(category));

        List<CategoryQueryPort.CategoryData> result =
                adapter.findCategoriesWithBudgetByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).budgetLimit())
                .isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Should return empty list when no categories")
    void shouldReturnEmptyWhenNoCategories() {
        when(categoryRepository.findByUserId(USER_ID))
                .thenReturn(Collections.emptyList());

        List<CategoryQueryPort.CategoryData> result =
                adapter.findByUserId(USER_ID);

        assertThat(result).isEmpty();
    }
}