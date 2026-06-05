package com.puntomartinez.millete.categories.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private final UUID userId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    @Test
    void shouldCreateCategory() {
        RegisterCategoryCommand command = new RegisterCategoryCommand(
                userId, "Transporte", "#FF0000", new BigDecimal("100.00"));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.register(command);

        assertThat(result.getName()).isEqualTo("Transporte");
        assertThat(result.getColor()).isEqualTo("#FF0000");
        assertThat(result.getBudgetLimit()).isEqualByComparingTo("100.00");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.isActive()).isTrue();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldFindByUserId() {
        Category cat1 = createCategory(userId, "Transporte");
        Category cat2 = createCategory(userId, "Hogar");
        Category cat3 = createInactiveCategory(userId, "Ocio");

        when(categoryRepository.findByIdUsuario(userId))
                .thenReturn(List.of(cat1, cat2, cat3));

        List<Category> result = categoryService.findByUserId(userId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName)
                .containsExactly("Transporte", "Hogar");
    }

    @Test
    void shouldUpdateCategory() {
        UUID id = UUID.randomUUID();
        Category existing = createCategory(userId, "Transporte");
        UpdateCategoryCommand command = new UpdateCategoryCommand(
                "Hogar", "#00FF00", new BigDecimal("200.00"));

        when(categoryRepository.findByIdAndUserId(id, userId))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.update(id, userId, command);

        assertThat(result.getName()).isEqualTo("Hogar");
        assertThat(result.getColor()).isEqualTo("#00FF00");
        assertThat(result.getBudgetLimit()).isEqualByComparingTo("200.00");
        verify(categoryRepository).findByIdAndUserId(id, userId);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistent() {
        UUID id = UUID.randomUUID();
        UpdateCategoryCommand command = new UpdateCategoryCommand(
                "Hogar", "#00FF00", null);

        when(categoryRepository.findByIdAndUserId(id, userId))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> categoryService.update(id, userId, command))
                .withMessageContaining("Categoría no encontrada");
    }

    @Test
    void shouldThrowWhenUpdatingCategoryOfOtherUser() {
        UUID id = UUID.randomUUID();
        Category existing = createCategory(userId, "Transporte");
        UpdateCategoryCommand command = new UpdateCategoryCommand(
                "Hogar", "#00FF00", null);

        // Otro usuario intenta actualizar
        when(categoryRepository.findByIdAndUserId(id, otherUserId))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> categoryService.update(id, otherUserId, command))
                .withMessageContaining("Categoría no encontrada");

        // Verificar que nunca se llamó a save (no se modificó nada)
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCategory() {
        UUID id = UUID.randomUUID();
        Category existing = createCategory(userId, "Transporte");

        Transaction tx1 = mock(Transaction.class);
        when(tx1.getUserId()).thenReturn(userId);

        Transaction tx2 = mock(Transaction.class);
        when(tx2.getUserId()).thenReturn(userId);

        when(categoryRepository.findByIdAndUserId(id, userId))
                .thenReturn(Optional.of(existing));
        when(transactionRepository.findAllByCategoryId(id))
                .thenReturn(List.of(tx1, tx2));

        categoryService.delete(id, userId);

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
    void shouldDeleteCategoryAndOnlyModifyOwnerTransactions() {
        UUID id = UUID.randomUUID();
        Category existing = createCategory(userId, "Transporte");

        Transaction ownerTx = mock(Transaction.class);
        when(ownerTx.getUserId()).thenReturn(userId);

        Transaction otherTx = mock(Transaction.class);
        when(otherTx.getUserId()).thenReturn(otherUserId); // Transacción de otro usuario

        when(categoryRepository.findByIdAndUserId(id, userId))
                .thenReturn(Optional.of(existing));
        when(transactionRepository.findAllByCategoryId(id))
                .thenReturn(List.of(ownerTx, otherTx));

        categoryService.delete(id, userId);

        // Solo la transacción del propietario se modifica
        verify(ownerTx).setCategoryId(null);
        verify(ownerTx).setModifiedAt(any());
        verify(transactionRepository).save(ownerTx);

        // La transacción del otro usuario NO se toca
        verify(otherTx, never()).setCategoryId(any());
        verify(otherTx, never()).setModifiedAt(any());
        verify(transactionRepository, never()).save(otherTx);
    }

    @Test
    void shouldThrowWhenDeletingNonExistent() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findByIdAndUserId(id, userId))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> categoryService.delete(id, userId))
                .withMessageContaining("Categoría no encontrada");
    }

    @Test
    void shouldThrowWhenDeletingCategoryOfOtherUser() {
        UUID id = UUID.randomUUID();

        // Otro usuario intenta eliminar
        when(categoryRepository.findByIdAndUserId(id, otherUserId))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> categoryService.delete(id, otherUserId))
                .withMessageContaining("Categoría no encontrada");

        verify(transactionRepository, never()).findAllByCategoryId(any());
        verify(categoryRepository, never()).save(any());
    }

    // Métodos helper

    private Category createCategory(UUID userId, String name) {
        return new Category(userId, name, "#FF0000", BigDecimal.ZERO);
    }

    private Category createInactiveCategory(UUID userId, String name) {
        Category cat = new Category(userId, name, "#FF0000", BigDecimal.ZERO);
        cat.deactivate();
        return cat;
    }
}