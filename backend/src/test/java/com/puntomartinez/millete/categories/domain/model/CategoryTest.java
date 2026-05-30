package com.puntomartinez.millete.categories.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CategoryTest {

    private final UUID userId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void shouldCreateCategory() {
        Category cat = new Category(userId, "Transporte", "#FF0000", new BigDecimal("100.00"));

        assertThat(cat.getName()).isEqualTo("Transporte");
        assertThat(cat.getColor()).isEqualTo("#FF0000");
        assertThat(cat.getBudgetLimit()).isEqualByComparingTo("100.00");
        assertThat(cat.isActive()).isTrue();
        assertThat(cat.getId()).isNotNull();
        assertThat(cat.getUserId()).isEqualTo(userId);
        assertThat(cat.getCreatedAt()).isNotNull();
        assertThat(cat.getModifiedAt()).isNotNull();
    }

    @Test
    void shouldAcceptNullBudgetLimit() {
        Category cat = new Category(userId, "Transporte", "#FF0000", null);
        assertThat(cat.getBudgetLimit()).isNull();
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Category(userId, "", "#FF0000", null));
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Category(userId, null, "#FF0000", null));
    }

    @Test
    void shouldThrowWhenColorIsInvalid() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Category(userId, "Transporte", "rojo", null))
                .withMessage("El color debe ser un hexadecimal válido (ej: #FF5733)");
    }

    @Test
    void shouldThrowWhenColorIsInvalidFormat() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Category(userId, "Transporte", "#FFF", null));
    }

    @Test
    void shouldThrowWhenBudgetLimitIsNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Category(userId, "Transporte", "#FF0000", new BigDecimal("-100.00")))
                .withMessage("El límite de presupuesto no puede ser negativo");
    }

    @Test
    void shouldUpdateDetails() {
        Category cat = new Category(userId, "Transporte", "#FF0000", new BigDecimal("100.00"));
        LocalDateTime beforeUpdate = cat.getModifiedAt();

        cat.updateDetails("Hogar", "#00FF00", new BigDecimal("200.00"));

        assertThat(cat.getName()).isEqualTo("Hogar");
        assertThat(cat.getColor()).isEqualTo("#00FF00");
        assertThat(cat.getBudgetLimit()).isEqualByComparingTo("200.00");
        assertThat(cat.getModifiedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    void shouldThrowWhenUpdatingWithBlankName() {
        Category cat = new Category(userId, "Transporte", "#FF0000", null);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> cat.updateDetails("", "#00FF00", null));
    }

    @Test
    void shouldThrowWhenUpdatingWithInvalidColor() {
        Category cat = new Category(userId, "Transporte", "#FF0000", null);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> cat.updateDetails("Hogar", "rojo", null));
    }

    @Test
    void shouldThrowWhenUpdatingWithNegativeBudget() {
        Category cat = new Category(userId, "Transporte", "#FF0000", new BigDecimal("100"));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> cat.updateDetails("Hogar", "#00FF00", new BigDecimal("-50")));
    }

    @Test
    void shouldDeactivate() {
        Category cat = new Category(userId, "Transporte", "#FF0000", null);
        LocalDateTime beforeDeactivate = cat.getModifiedAt();

        cat.deactivate();

        assertThat(cat.isActive()).isFalse();
        assertThat(cat.getModifiedAt()).isAfterOrEqualTo(beforeDeactivate);
    }
}