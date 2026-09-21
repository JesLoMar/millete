package com.puntomartinez.millete.plannedtransactions.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryDisplayPort;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryDisplayPort.CategoryDisplay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryDisplayAdapter")
class CategoryDisplayAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryDisplayAdapter adapter;

    private Category category() {
        return Category.reconstitute(
                CATEGORY_ID, USER_ID, "Food", "#FF0000",
                new BigDecimal("100.00"),
                LocalDateTime.now(), LocalDateTime.now(), true
        );
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        @DisplayName("Should map category to display when found")
        void shouldMapCategoryToDisplayWhenFound() {
            when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                    .thenReturn(Optional.of(category()));

            Optional<CategoryDisplay> result =
                    adapter.findByIdAndUserId(CATEGORY_ID, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(CATEGORY_ID);
            assertThat(result.get().name()).isEqualTo("Food");
            assertThat(result.get().color()).isEqualTo("#FF0000");
        }

        @Test
        @DisplayName("Should return empty when category not found")
        void shouldReturnEmptyWhenCategoryNotFound() {
            when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                    .thenReturn(Optional.empty());

            Optional<CategoryDisplay> result =
                    adapter.findByIdAndUserId(CATEGORY_ID, USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("Should map categories to display list")
        void shouldMapCategoriesToDisplayList() {
            when(categoryRepository.findByUserId(USER_ID))
                    .thenReturn(List.of(category()));

            List<CategoryDisplay> result = adapter.findByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Food");
        }

        @Test
        @DisplayName("Should return empty list when no categories")
        void shouldReturnEmptyListWhenNoCategories() {
            when(categoryRepository.findByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            List<CategoryDisplay> result = adapter.findByUserId(USER_ID);

            assertThat(result).isEmpty();
        }
    }
}