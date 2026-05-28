package com.puntomartinez.millete.categories.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService - Servicio de categorías")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Crear categoría con datos válidos")
    void shouldCreateCategory() {
        RegisterCategoryUseCase.RegisterCategoryCommand command = new RegisterCategoryUseCase.RegisterCategoryCommand(
                userId, "Transporte", "#FF0000", new BigDecimal("100.00"));

        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.register(command);

        assertThat(result.getName()).isEqualTo("Transporte");
        assertThat(result.getColor()).isEqualTo("#FF0000");
        assertThat(result.getBudgetLimit()).isEqualByComparingTo("100.00");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.isActive()).isTrue();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Listar categorías por usuario")
    void shouldFindByUserId() {
        Category cat1 = createCategory("Transporte");
        Category cat2 = createCategory("Hogar");
        Category cat3 = createInactiveCategory("Ocio");

        when(categoryRepository.findByIdUsuario(userId)).thenReturn(List.of(cat1, cat2, cat3));

        List<Category> result = categoryService.findByUserId(userId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName).containsExactly("Transporte", "Hogar");
    }

    @Test
    @DisplayName("Actualizar categoría")
    void shouldUpdateCategory() {
        UUID id = UUID.randomUUID();
        Category existing = createCategory("Transporte");
        UpdateCategoryUseCase.UpdateCategoryCommand command = new UpdateCategoryUseCase.UpdateCategoryCommand(
                "Hogar", "#00FF00", new BigDecimal("200.00"));

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.update(id, command);

        assertThat(result.getName()).isEqualTo("Hogar");
        assertThat(result.getColor()).isEqualTo("#00FF00");
        assertThat(result.getBudgetLimit()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Actualizar categoría inexistente lanza error")
    void shouldThrowWhenUpdatingNonExistent() {
        UUID id = UUID.randomUUID();
        UpdateCategoryUseCase.UpdateCategoryCommand command = new UpdateCategoryUseCase.UpdateCategoryCommand(
                "Hogar", "#00FF00", new BigDecimal("200.00"));

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatRuntimeException()
                .isThrownBy(() -> categoryService.update(id, command))
                .withMessage("Categoría no encontrada");
    }

    @Test
    @DisplayName("Eliminar categoría desactiva y desvincula transacciones")
    void shouldDeleteCategory() {
        UUID id = UUID.randomUUID();
        Category existing = createCategory("Transporte");
        Transaction tx1 = mock(Transaction.class);
        Transaction tx2 = mock(Transaction.class);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(transactionRepository.findAllByCategoryId(id)).thenReturn(List.of(tx1, tx2));

        categoryService.delete(id);

        assertThat(existing.isActive()).isFalse();
        verify(tx1).setCategoryId(null);
        verify(tx1).setModifiedAt(any());
        verify(tx2).setCategoryId(null);
        verify(tx2).setModifiedAt(any());
        verify(transactionRepository).save(tx1);
        verify(transactionRepository).save(tx2);
        verify(categoryRepository).save(existing);
    }

    @Test
    @DisplayName("Eliminar categoría inexistente lanza error")
    void shouldThrowWhenDeletingNonExistent() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatRuntimeException()
                .isThrownBy(() -> categoryService.delete(id))
                .withMessage("Categoría no encontrada");
    }

    private Category createCategory(String name) {
        return new Category(userId, name, "#FF0000", BigDecimal.ZERO);
    }

    private Category createInactiveCategory(String name) {
        Category cat = new Category(userId, name, "#FF0000", BigDecimal.ZERO);
        cat.deactivate();
        return cat;
    }
}