package com.puntomartinez.millete.categories.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.transactions.domain.ports.in.UnassignCategoryFromTransactionsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

@DisplayName("CategoryService")
class CategoryServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final FixedTimeProvider TIME =
            new FixedTimeProvider(Instant.parse("2024-01-01T10:00:00Z"));
    private static final String UNIQUE_INDEX_NAME = "idx_categories_user_name_active";

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UnassignCategoryFromTransactionsUseCase unassignCategoryFromTransactionsUseCase;

    @InjectMocks
    private CategoryService categoryService;

    private RegisterCategoryCommand registerCommand() {
        return new RegisterCategoryCommand(
                USER_ID,
                "Food",
                "#FF5733",
                BigDecimal.TEN
        );
    }

    private Category existingCategory() {
        return Category.create(TIME, 
                TIME,
                USER_ID,
                "Old",
                "#FF5733",
                BigDecimal.TEN
        );
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("Should register category when name does not exist")
        void shouldRegisterCategoryWhenNameDoesNotExist() {
            RegisterCategoryCommand command = registerCommand();

            when(categoryRepository.existsActiveByUserIdAndName(USER_ID, command.name()))
                    .thenReturn(false);
            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.register(command);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryRepository).save(captor.capture());

            Category saved = captor.getValue();

            assertThat(saved.getUserId()).isEqualTo(USER_ID);
            assertThat(saved.getName()).isEqualTo("Food");
            assertThat(saved.getColor()).isEqualTo("#FF5733");
            assertThat(saved.getBudgetLimit()).isEqualByComparingTo(BigDecimal.TEN);
            assertThat(saved.isActive()).isTrue();
            assertThat(result).isSameAs(saved);
        }

        @Test
        @DisplayName("Should throw when active category with same name already exists")
        void shouldThrowWhenActiveCategoryWithSameNameAlreadyExists() {
            RegisterCategoryCommand command = registerCommand();

            when(categoryRepository.existsActiveByUserIdAndName(USER_ID, command.name()))
                    .thenReturn(true);

            assertThatThrownBy(() -> categoryService.register(command))
                    .isInstanceOf(ResourceAlreadyExistsException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should convert data integrity violation into resource already exists when unique index is present")
        void shouldConvertDataIntegrityViolationIntoResourceAlreadyExistsWhenUniqueIndexIsPresent() {
            RegisterCategoryCommand command = registerCommand();
            DataIntegrityViolationException duplicateException =
                    new DataIntegrityViolationException(
                            "Duplicate category",
                            new RuntimeException(UNIQUE_INDEX_NAME)
                    );

            when(categoryRepository.existsActiveByUserIdAndName(USER_ID, command.name()))
                    .thenReturn(false);
            when(categoryRepository.save(any(Category.class)))
                    .thenThrow(duplicateException);

            assertThatThrownBy(() -> categoryService.register(command))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasCause(duplicateException);
        }

        @Test
        @DisplayName("Should rethrow data integrity violation when it is not related to unique category name index")
        void shouldRethrowDataIntegrityViolationWhenNotRelatedToUniqueIndex() {
            RegisterCategoryCommand command = registerCommand();
            DataIntegrityViolationException otherException =
                    new DataIntegrityViolationException(
                            "Other integrity error",
                            new RuntimeException("some_other_constraint")
                    );

            when(categoryRepository.existsActiveByUserIdAndName(USER_ID, command.name()))
                    .thenReturn(false);
            when(categoryRepository.save(any(Category.class)))
                    .thenThrow(otherException);

            assertThatThrownBy(() -> categoryService.register(command))
                    .isSameAs(otherException);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update category when it exists and name is not taken by another category")
        void shouldUpdateCategoryWhenItExistsAndNameIsAvailable() {
            Category category = existingCategory();
            UpdateCategoryCommand command =
                    new UpdateCategoryCommand("New", "#00FF00", BigDecimal.ONE);

            when(categoryRepository.findByIdAndUserId(category.getId(), USER_ID))
                    .thenReturn(Optional.of(category));
            when(categoryRepository.existsActiveByUserIdAndNameExcludingId(
                    USER_ID,
                    command.name(),
                    category.getId()
            )).thenReturn(false);
            when(categoryRepository.save(category)).thenReturn(category);

            Category result = categoryService.update(category.getId(), USER_ID, command);

            assertThat(result.getName()).isEqualTo("New");
            assertThat(result.getColor()).isEqualTo("#00FF00");
            assertThat(result.getBudgetLimit()).isEqualByComparingTo(BigDecimal.ONE);
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should throw when category does not exist")
        void shouldThrowWhenCategoryDoesNotExist() {
            UUID categoryId = UUID.randomUUID();
            UpdateCategoryCommand command =
                    new UpdateCategoryCommand("New", "#00FF00", BigDecimal.ONE);

            when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(categoryId, USER_ID, command))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository, never()).save(any(Category.class));
            verify(categoryRepository, never())
                    .existsActiveByUserIdAndNameExcludingId(any(), any(), any());
        }

        @Test
        @DisplayName("Should throw when another active category already uses the new name")
        void shouldThrowWhenAnotherActiveCategoryAlreadyUsesNewName() {
            Category category = existingCategory();
            UpdateCategoryCommand command =
                    new UpdateCategoryCommand("New", "#00FF00", BigDecimal.ONE);

            when(categoryRepository.findByIdAndUserId(category.getId(), USER_ID))
                    .thenReturn(Optional.of(category));
            when(categoryRepository.existsActiveByUserIdAndNameExcludingId(
                    USER_ID,
                    command.name(),
                    category.getId()
            )).thenReturn(true);

            assertThatThrownBy(() -> categoryService.update(category.getId(), USER_ID, command))
                    .isInstanceOf(ResourceAlreadyExistsException.class);

            verify(categoryRepository, never()).save(any(Category.class));
            assertThat(category.getName()).isEqualTo("Old");
        }

        @Test
        @DisplayName("Should convert data integrity violation into resource already exists on update")
        void shouldConvertDataIntegrityViolationIntoResourceAlreadyExistsOnUpdate() {
            Category category = existingCategory();
            UpdateCategoryCommand command =
                    new UpdateCategoryCommand("New", "#00FF00", BigDecimal.ONE);
            DataIntegrityViolationException duplicateException =
                    new DataIntegrityViolationException(
                            "Duplicate category",
                            new RuntimeException(UNIQUE_INDEX_NAME)
                    );

            when(categoryRepository.findByIdAndUserId(category.getId(), USER_ID))
                    .thenReturn(Optional.of(category));
            when(categoryRepository.existsActiveByUserIdAndNameExcludingId(
                    USER_ID,
                    command.name(),
                    category.getId()
            )).thenReturn(false);
            when(categoryRepository.save(category)).thenThrow(duplicateException);

            assertThatThrownBy(() -> categoryService.update(category.getId(), USER_ID, command))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasCause(duplicateException);
        }

        @Test
        @DisplayName("Should rethrow data integrity violation when it is not related to unique category name index on update")
        void shouldRethrowDataIntegrityViolationWhenNotRelatedToUniqueIndexOnUpdate() {
            Category category = existingCategory();
            UpdateCategoryCommand command =
                    new UpdateCategoryCommand("New", "#00FF00", BigDecimal.ONE);
            DataIntegrityViolationException otherException =
                    new DataIntegrityViolationException(
                            "Other integrity error",
                            new RuntimeException("some_other_constraint")
                    );

            when(categoryRepository.findByIdAndUserId(category.getId(), USER_ID))
                    .thenReturn(Optional.of(category));
            when(categoryRepository.existsActiveByUserIdAndNameExcludingId(
                    USER_ID,
                    command.name(),
                    category.getId()
            )).thenReturn(false);
            when(categoryRepository.save(category)).thenThrow(otherException);

            assertThatThrownBy(() -> categoryService.update(category.getId(), USER_ID, command))
                    .isSameAs(otherException);
        }
    }

    @Nested
    @DisplayName("deleteByIdAndUserId")
    class Delete {

        @Test
        @DisplayName("Should deactivate category and unassign it from transactions")
        void shouldDeactivateCategoryAndUnassignFromTransactions() {
            Category category = existingCategory();

            when(categoryRepository.findByIdAndUserId(category.getId(), USER_ID))
                    .thenReturn(Optional.of(category));

            categoryService.deleteByIdAndUserId(category.getId(), USER_ID);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            InOrder inOrder = inOrder(categoryRepository, unassignCategoryFromTransactionsUseCase);

            inOrder.verify(categoryRepository).save(captor.capture());
            inOrder.verify(unassignCategoryFromTransactionsUseCase)
                    .unassignCategory(category.getId(), USER_ID);

            assertThat(captor.getValue().isActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw when category does not exist")
        void shouldThrowWhenCategoryDoesNotExist() {
            UUID categoryId = UUID.randomUUID();

            when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteByIdAndUserId(categoryId, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository, never()).save(any(Category.class));
            verify(unassignCategoryFromTransactionsUseCase, never())
                    .unassignCategory(any(), any());
        }
    }

    @Nested
    @DisplayName("queries")
    class Queries {

        @Test
        @DisplayName("Should return category when findByIdAndUserId finds it")
        void shouldReturnCategoryWhenFindByIdAndUserIdFindsIt() {
            Category category = existingCategory();

            when(categoryRepository.findByIdAndUserId(category.getId(), USER_ID))
                    .thenReturn(Optional.of(category));

            Category result = categoryService.findByIdAndUserId(category.getId(), USER_ID);

            assertThat(result).isSameAs(category);
        }

        @Test
        @DisplayName("Should throw when findByIdAndUserId does not find category")
        void shouldThrowWhenFindByIdAndUserIdDoesNotFindCategory() {
            UUID categoryId = UUID.randomUUID();

            when(categoryRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findByIdAndUserId(categoryId, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should delegate findByUserId to repository")
        void shouldDelegateFindByUserIdToRepository() {
            Category category = existingCategory();
            List<Category> categories = List.of(category);

            when(categoryRepository.findByUserId(USER_ID)).thenReturn(categories);

            List<Category> result = categoryService.findByUserId(USER_ID);

            assertThat(result).containsExactly(category);
        }

        @Test
        @DisplayName("Should delegate findAllByUserId to repository")
        void shouldDelegateFindAllByUserIdToRepository() {
            Category category = existingCategory();
            List<Category> categories = List.of(category);

            when(categoryRepository.findAllByUserId(USER_ID, 0, 10, "food"))
                    .thenReturn(categories);

            List<Category> result = categoryService.findAllByUserId(USER_ID, 0, 10, "food");

            assertThat(result).containsExactly(category);
        }

        @Test
        @DisplayName("Should delegate countByUserIdAndFilters to repository")
        void shouldDelegateCountByUserIdAndFiltersToRepository() {
            when(categoryRepository.countByUserIdAndFilters(USER_ID, "food"))
                    .thenReturn(5L);

            long result = categoryService.countByUserIdAndFilters(USER_ID, "food");

            assertThat(result).isEqualTo(5L);
        }
    }
}