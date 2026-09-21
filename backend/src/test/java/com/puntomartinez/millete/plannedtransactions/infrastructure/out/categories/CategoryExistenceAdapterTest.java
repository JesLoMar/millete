package com.puntomartinez.millete.plannedtransactions.infrastructure.out.categories;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryExistenceAdapter")
class CategoryExistenceAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryExistenceAdapter adapter;

    @Test
    @DisplayName("Should return true when category exists for user")
    void shouldReturnTrueWhenCategoryExistsForUser() {
        Category category = Category.reconstitute(
                CATEGORY_ID, USER_ID, "Food", "#FF0000",
                new BigDecimal("100.00"),
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.of(category));

        boolean result = adapter.existsForUser(CATEGORY_ID, USER_ID);

        assertThat(result).isTrue();
        verify(categoryRepository).findByIdAndUserId(CATEGORY_ID, USER_ID);
    }

    @Test
    @DisplayName("Should return false when category does not exist")
    void shouldReturnFalseWhenCategoryDoesNotExist() {
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.empty());

        boolean result = adapter.existsForUser(CATEGORY_ID, USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should delegate to repository")
    void shouldDelegateToRepository() {
        UUID categoryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        adapter.existsForUser(categoryId, userId);

        verify(categoryRepository).findByIdAndUserId(categoryId, userId);
    }
}