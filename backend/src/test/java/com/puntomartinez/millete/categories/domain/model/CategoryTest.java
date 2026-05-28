package com.puntomartinez.millete.categories.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Category - Modelo de dominio")
class CategoryTest {

    private final UUID userId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("Debe crear categoría con datos válidos")
    void shouldCreateCategory() {
        Category cat = new Category(userId, "Transporte", "#FF0000", new BigDecimal("100.00"));

        assertThat(cat.getName()).isEqualTo("Transporte");
        assertThat(cat.getColor()).isEqualTo("#FF0000");
        assertThat(cat.getBudgetLimit()).isEqualByComparingTo("100.00");
        assertThat(cat.isActive()).isTrue();
        assertThat(cat.getId()).isNotNull();
    }

    @Test
    @DisplayName("Debe asignar color blanco por defecto si es nulo")
    void shouldAssignDefaultColor() {
        Category cat = new Category(userId, "Transporte", null, null);
        assertThat(cat.getColor()).isEqualTo("#FFFFFF");
    }

    @Test
    @DisplayName("Debe asignar presupuesto cero por defecto")
    void shouldAssignDefaultBudget() {
        Category cat = new Category(userId, "Transporte", "#FF0000", null);
        assertThat(cat.getBudgetLimit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debe lanzar error si nombre está vacío")
    void shouldThrowWhenNameIsBlank() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Category(userId, "", "#FF0000", null));
    }

    @Test
    @DisplayName("Debe actualizar detalles correctamente")
    void shouldUpdateDetails() {
        Category cat = new Category(userId, "Transporte", "#FF0000", new BigDecimal("100.00"));
        cat.updateDetails("Hogar", "#00FF00", new BigDecimal("200.00"));

        assertThat(cat.getName()).isEqualTo("Hogar");
        assertThat(cat.getColor()).isEqualTo("#00FF00");
        assertThat(cat.getBudgetLimit()).isEqualByComparingTo("200.00");
        assertThat(cat.getModifiedAt()).isAfterOrEqualTo(now);
    }

    @Test
    @DisplayName("Debe desactivar categoría")
    void shouldDeactivate() {
        Category cat = new Category(userId, "Transporte", "#FF0000", null);
        cat.deactivate();

        assertThat(cat.isActive()).isFalse();
        assertThat(cat.getModifiedAt()).isAfterOrEqualTo(now);
    }
}