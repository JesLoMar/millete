package com.puntomartinez.millete.plannedtransactions.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryDisplayAdapter")
class CategoryDisplayAdapterTest {
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryDisplayAdapter adapter;

    private Category buildCategory(UUID id) {
        return Category.reconstitute(
                id, USER_ID, "Food", "#FF0000",
                new BigDecimal("500.00"),
                LocalDateTime.now(), LocalDateTime.now(), true
        );
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {
        @Test
        @DisplayName("Should map to CategoryDisplay when found")
        void shouldMapWhenFound() {
            UUID categoryId = UUID.randomUUID();
            Category category = buildCategory(categoryId);
            when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.of(category));

            Optional<CategoryDisplay> result =
                    adapter.findByIdAndUserId(categoryId, USER_ID);

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(categoryId);
            assertThat(result.get().name()).isEqualTo("Food");
            assertThat(result.get().color()).isEqualTo("#FF0000");
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            UUID categoryId = UUID.randomUUID();
            when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.empty());

            Optional<CategoryDisplay> result =
                    adapter.findByIdAndUserId(categoryId, USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {
        @Test
        @DisplayName("Should map all categories to CategoryDisplay list")
        void shouldMapAll() {
            Category c1 = buildCategory(UUID.randomUUID());
            Category c2 = buildCategory(UUID.randomUUID());
            when(categoryRepository.findByUserId(USER_ID))
                    .thenReturn(List.of(c1, c2));

            List<CategoryDisplay> result = adapter.findByUserId(USER_ID);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no categories")
        void shouldReturnEmpty() {
            when(categoryRepository.findByUserId(USER_ID))
                    .thenReturn(List.of());

            List<CategoryDisplay> result = adapter.findByUserId(USER_ID);

            assertThat(result).isEmpty();
        }
    }
}