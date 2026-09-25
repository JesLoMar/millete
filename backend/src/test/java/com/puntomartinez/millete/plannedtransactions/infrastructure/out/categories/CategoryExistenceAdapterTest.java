package com.puntomartinez.millete.plannedtransactions.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryExistenceAdapter")
class CategoryExistenceAdapterTest {
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryExistenceAdapter adapter;

    @Nested
    @DisplayName("existsForUser")
    class ExistsForUser {
        @Test
        @DisplayName("Should return true when category exists")
        void shouldReturnTrue() {
            UUID categoryId = UUID.randomUUID();
            Category category = Category.reconstitute(
                    categoryId, USER_ID, "Food", "#FF0000",
                    new BigDecimal("100.00"),
                    Instant.now(), Instant.now(), true
            );
            when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.of(category));

            boolean result = adapter.existsForUser(categoryId, USER_ID);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when category does not exist")
        void shouldReturnFalse() {
            UUID categoryId = UUID.randomUUID();
            when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.empty());

            boolean result = adapter.existsForUser(categoryId, USER_ID);

            assertThat(result).isFalse();
        }
    }
}