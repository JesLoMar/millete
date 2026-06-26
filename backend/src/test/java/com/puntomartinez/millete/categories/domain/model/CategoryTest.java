package com.puntomartinez.millete.categories.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Category - Modelo de dominio")
class CategoryTest {

    @Test
    @DisplayName("Constructor con validación debe crear categoría")
    void constructorWithValidation_shouldCreateCategory() {
        UUID userId = UUID.randomUUID();
        Category cat = new Category(userId, "Comida", "#FF5733", new BigDecimal("500.00"));

        assertNotNull(cat.getId());
        assertEquals(userId, cat.getUserId());
        assertEquals("Comida", cat.getName());
        assertEquals("#FF5733", cat.getColor());
        assertEquals(new BigDecimal("500.00"), cat.getBudgetLimit());
        assertTrue(cat.isActive());
        assertNotNull(cat.getCreatedAt());
        assertNotNull(cat.getModifiedAt());
    }

    @Test
    @DisplayName("Constructor con validación debe permitir budgetLimit nulo")
    void constructorWithValidation_shouldAllowNullBudgetLimit() {
        Category cat = new Category(UUID.randomUUID(), "Comida", "#FF5733", null);
        assertNull(cat.getBudgetLimit());
    }

    @Test
    @DisplayName("Constructor con validación debe lanzar error con nombre vacío")
    void constructorWithValidation_shouldThrow_whenNameBlank() {
        assertThrows(IllegalArgumentException.class, () ->
            new Category(UUID.randomUUID(), "", "#FF5733", null)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new Category(UUID.randomUUID(), null, "#FF5733", null)
        );
    }

    @Test
    @DisplayName("Constructor con validación debe lanzar error con color inválido")
    void constructorWithValidation_shouldThrow_whenColorInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
            new Category(UUID.randomUUID(), "Comida", "INVALID", null)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new Category(UUID.randomUUID(), "Comida", null, null)
        );
    }

    @Test
    @DisplayName("Constructor con validación debe lanzar error con budgetLimit negativo")
    void constructorWithValidation_shouldThrow_whenBudgetLimitNegative() {
        assertThrows(IllegalArgumentException.class, () ->
            new Category(UUID.randomUUID(), "Comida", "#FF5733", new BigDecimal("-10.00"))
        );
    }

    @Test
    @DisplayName("Constructor completo debe asignar todos los valores")
    void fullConstructor_shouldAssignAllValues() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Category cat = new Category(id, userId, "Comida", "#FF5733",
                new BigDecimal("500.00"), null, null, true);

        assertEquals(id, cat.getId());
        assertEquals(userId, cat.getUserId());
        assertEquals("Comida", cat.getName());
        assertEquals("#FF5733", cat.getColor());
        assertEquals(new BigDecimal("500.00"), cat.getBudgetLimit());
        assertTrue(cat.isActive());
    }

    @Test
    @DisplayName("updateDetails debe actualizar campos permitidos")
    void updateDetails_shouldUpdateFields() {
        Category cat = new Category(UUID.randomUUID(), "Comida", "#FF5733", new BigDecimal("500.00"));

        cat.updateDetails("Nueva comida", "#00FF00", new BigDecimal("1000.00"));

        assertEquals("Nueva comida", cat.getName());
        assertEquals("#00FF00", cat.getColor());
        assertEquals(new BigDecimal("1000.00"), cat.getBudgetLimit());
        assertNotNull(cat.getModifiedAt());
    }

    @Test
    @DisplayName("updateDetails debe lanzar error con nombre vacío")
    void updateDetails_shouldThrow_whenNameBlank() {
        Category cat = new Category(UUID.randomUUID(), "Comida", "#FF5733", null);

        assertThrows(IllegalArgumentException.class, () ->
            cat.updateDetails("", "#FF5733", null)
        );
        assertThrows(IllegalArgumentException.class, () ->
            cat.updateDetails(null, "#FF5733", null)
        );
    }

    @Test
    @DisplayName("updateDetails debe lanzar error con color inválido")
    void updateDetails_shouldThrow_whenColorInvalid() {
        Category cat = new Category(UUID.randomUUID(), "Comida", "#FF5733", null);

        assertThrows(IllegalArgumentException.class, () ->
            cat.updateDetails("Comida", "INVALID", null)
        );
    }

    @Test
    @DisplayName("updateDetails debe lanzar error con budgetLimit negativo")
    void updateDetails_shouldThrow_whenBudgetLimitNegative() {
        Category cat = new Category(UUID.randomUUID(), "Comida", "#FF5733", null);

        assertThrows(IllegalArgumentException.class, () ->
            cat.updateDetails("Comida", "#FF5733", new BigDecimal("-10.00"))
        );
    }

    @Test
    @DisplayName("deactivate debe marcar como inactivo")
    void deactivate_shouldMarkInactive() {
        Category cat = new Category(UUID.randomUUID(), "Comida", "#FF5733", null);
        assertTrue(cat.isActive());

        cat.deactivate();

        assertFalse(cat.isActive());
        assertNotNull(cat.getModifiedAt());
    }

    @Test
    @DisplayName("Setters y getters deben funcionar correctamente")
    void settersAndGetters_shouldWork() {
        Category cat = new Category();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        cat.setId(id);
        cat.setUserId(userId);
        cat.setName("Transporte");
        cat.setColor("#0000FF");
        cat.setBudgetLimit(new BigDecimal("200.00"));
        cat.setActive(false);

        assertEquals(id, cat.getId());
        assertEquals(userId, cat.getUserId());
        assertEquals("Transporte", cat.getName());
        assertEquals("#0000FF", cat.getColor());
        assertEquals(new BigDecimal("200.00"), cat.getBudgetLimit());
        assertFalse(cat.isActive());
    }
}
