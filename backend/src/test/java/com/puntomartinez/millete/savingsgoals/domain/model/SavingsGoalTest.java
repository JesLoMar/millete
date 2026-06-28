package com.puntomartinez.millete.savingsgoals.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SavingsGoal - Modelo de dominio")
class SavingsGoalTest {

    @Test
    @DisplayName("Constructor por defecto debe inicializar valores")
    void defaultConstructor_shouldInitializeValues() {
        SavingsGoal goal = new SavingsGoal();

        assertNotNull(goal.getId());
        assertEquals(BigDecimal.ZERO, goal.getCurrentAmount());
        assertEquals("MEDIUM", goal.getPriority());
        assertEquals("ACTIVE", goal.getStatus());
        assertNotNull(goal.getCreatedAt());
        assertNotNull(goal.getModifiedAt());
        assertTrue(goal.isActive());
    }

    @Test
    @DisplayName("Constructor completo debe asignar todos los valores")
    void fullConstructor_shouldAssignAllValues() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate deadline = LocalDate.now().plusMonths(6);
        LocalDateTime now = LocalDateTime.now();

        SavingsGoal goal = new SavingsGoal(
                id, userId, "Vacaciones",
                new BigDecimal("2000.00"), new BigDecimal("500.00"),
                deadline, "HIGH", "ACTIVE", "http://link.com",
                now, now, true
        );

        assertEquals(id, goal.getId());
        assertEquals(userId, goal.getUserId());
        assertEquals("Vacaciones", goal.getName());
        assertEquals(new BigDecimal("2000.00"), goal.getTargetAmount());
        assertEquals(new BigDecimal("500.00"), goal.getCurrentAmount());
        assertEquals(deadline, goal.getDeadline());
        assertEquals("HIGH", goal.getPriority());
        assertEquals("ACTIVE", goal.getStatus());
        assertEquals("http://link.com", goal.getLink());
        assertTrue(goal.isActive());
    }

    @Test
    @DisplayName("Constructor debe usar defaults para valores nulos")
    void fullConstructor_shouldUseDefaultsForNulls() {
        SavingsGoal goal = new SavingsGoal(
                null, null, "Test",
                new BigDecimal("1000.00"), null,
                null, null, null, null,
                null, null, true
        );

        assertNotNull(goal.getId());
        assertEquals(BigDecimal.ZERO, goal.getCurrentAmount());
        assertEquals("MEDIUM", goal.getPriority());
        assertEquals("ACTIVE", goal.getStatus());
        assertNotNull(goal.getCreatedAt());
        assertNotNull(goal.getModifiedAt());
    }

    @Test
    @DisplayName("addContribution debe aumentar currentAmount")
    void addContribution_shouldIncreaseCurrentAmount() {
        SavingsGoal goal = new SavingsGoal();
        goal.setTargetAmount(new BigDecimal("1000.00"));
        goal.setCurrentAmount(BigDecimal.ZERO);

        goal.addContribution(new BigDecimal("250.00"));

        assertEquals(new BigDecimal("250.00"), goal.getCurrentAmount());
    }

    @Test
    @DisplayName("addContribution debe cambiar estado a COMPLETED cuando se alcanza el objetivo")
    void addContribution_shouldChangeStatusToCompleted() {
        SavingsGoal goal = new SavingsGoal();
        goal.setTargetAmount(new BigDecimal("1000.00"));
        goal.setCurrentAmount(new BigDecimal("800.00"));
        goal.setStatus("ACTIVE");

        goal.addContribution(new BigDecimal("200.00"));

        assertEquals("COMPLETED", goal.getStatus());
        assertEquals(new BigDecimal("1000.00"), goal.getCurrentAmount());
    }

    @Test
    @DisplayName("addContribution debe lanzar error con monto nulo")
    void addContribution_shouldThrow_whenAmountNull() {
        SavingsGoal goal = new SavingsGoal();
        assertThrows(IllegalArgumentException.class, () -> goal.addContribution(null));
    }

    @Test
    @DisplayName("addContribution debe lanzar error con monto cero")
    void addContribution_shouldThrow_whenAmountZero() {
        SavingsGoal goal = new SavingsGoal();
        assertThrows(IllegalArgumentException.class, () -> goal.addContribution(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("addContribution debe lanzar error con monto negativo")
    void addContribution_shouldThrow_whenAmountNegative() {
        SavingsGoal goal = new SavingsGoal();
        assertThrows(IllegalArgumentException.class, () -> goal.addContribution(new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("deactivate debe marcar como inactivo y cambiar estado")
    void deactivate_shouldMarkInactive() {
        SavingsGoal goal = new SavingsGoal();
        goal.setActive(true);
        goal.setStatus("ACTIVE");

        goal.deactivate();

        assertFalse(goal.isActive());
        assertEquals("CANCELLED", goal.getStatus());
    }

    @Test
    @DisplayName("deactivate no debe cambiar estado si ya es COMPLETED")
    void deactivate_shouldNotChangeStatusIfCompleted() {
        SavingsGoal goal = new SavingsGoal();
        goal.setActive(true);
        goal.setStatus("COMPLETED");

        goal.deactivate();

        assertFalse(goal.isActive());
        assertEquals("COMPLETED", goal.getStatus());
    }

    @Test
    @DisplayName("updateDetails debe actualizar campos permitidos")
    void updateDetails_shouldUpdateFields() {
        SavingsGoal goal = new SavingsGoal();
        goal.setTargetAmount(new BigDecimal("1000.00"));
        goal.setStatus("ACTIVE");

        LocalDate newDeadline = LocalDate.now().plusMonths(12);
        goal.updateDetails("Nuevo nombre", new BigDecimal("2000.00"), newDeadline, "LOW", "PAUSED", "http://new.com");

        assertEquals("Nuevo nombre", goal.getName());
        assertEquals(new BigDecimal("2000.00"), goal.getTargetAmount());
        assertEquals(newDeadline, goal.getDeadline());
        assertEquals("LOW", goal.getPriority());
        assertEquals("PAUSED", goal.getStatus());
        assertEquals("http://new.com", goal.getLink());
    }

    @Test
    @DisplayName("updateDetails debe recalcular estado a COMPLETED si se alcanza el objetivo")
    void updateDetails_shouldRecalculateStatus() {
        SavingsGoal goal = new SavingsGoal();
        goal.setTargetAmount(new BigDecimal("1000.00"));
        goal.setCurrentAmount(new BigDecimal("500.00"));
        goal.setStatus("ACTIVE");

        goal.updateDetails(null, new BigDecimal("400.00"), null, null, null, null);

        assertEquals("COMPLETED", goal.getStatus());
    }

    @Test
    @DisplayName("Constructor debe lanzar error con nombre vacío")
    void constructor_shouldThrow_whenNameBlank() {
        assertThrows(IllegalArgumentException.class, () ->
            new SavingsGoal(UUID.randomUUID(), UUID.randomUUID(), "", new BigDecimal("1000.00"), null, null, null, null, null, null, null, true)
        );
    }

    @Test
    @DisplayName("Constructor debe lanzar error con targetAmount cero")
    void constructor_shouldThrow_whenTargetAmountZero() {
        assertThrows(IllegalArgumentException.class, () ->
            new SavingsGoal(UUID.randomUUID(), UUID.randomUUID(), "Test", BigDecimal.ZERO, null, null, null, null, null, null, null, true)
        );
    }

    @Test
    @DisplayName("Constructor debe lanzar error con currentAmount negativo")
    void constructor_shouldThrow_whenCurrentAmountNegative() {
        assertThrows(IllegalArgumentException.class, () ->
            new SavingsGoal(UUID.randomUUID(), UUID.randomUUID(), "Test", new BigDecimal("1000.00"), new BigDecimal("-10.00"), null, null, null, null, null, null, true)
        );
    }

    @Test
    @DisplayName("Constructor debe lanzar error con deadline pasada")
    void constructor_shouldThrow_whenDeadlinePast() {
        assertThrows(IllegalArgumentException.class, () ->
            new SavingsGoal(UUID.randomUUID(), UUID.randomUUID(), "Test", new BigDecimal("1000.00"), null, LocalDate.now().minusDays(1), null, null, null, null, null, true)
        );
    }

    @Test
    @DisplayName("Constructor debe lanzar error con prioridad inválida")
    void constructor_shouldThrow_whenPriorityInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
            new SavingsGoal(UUID.randomUUID(), UUID.randomUUID(), "Test", new BigDecimal("1000.00"), null, null, "INVALID", null, null, null, null, true)
        );
    }

    @Test
    @DisplayName("Constructor debe lanzar error con estado inválido")
    void constructor_shouldThrow_whenStatusInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
            new SavingsGoal(UUID.randomUUID(), UUID.randomUUID(), "Test", new BigDecimal("1000.00"), null, null, null, "INVALID", null, null, null, true)
        );
    }

    @Test
    @DisplayName("Setter y getter deben funcionar correctamente")
    void settersAndGetters_shouldWork() {
        SavingsGoal goal = new SavingsGoal();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        goal.setId(id);
        goal.setUserId(userId);
        goal.setName("Test");
        goal.setTargetAmount(new BigDecimal("1000.00"));
        goal.setCurrentAmount(new BigDecimal("500.00"));
        goal.setDeadline(LocalDate.now());
        goal.setPriority("HIGH");
        goal.setStatus("PAUSED");
        goal.setLink("http://test.com");
        goal.setActive(false);

        assertEquals(id, goal.getId());
        assertEquals(userId, goal.getUserId());
        assertEquals("Test", goal.getName());
        assertEquals(new BigDecimal("1000.00"), goal.getTargetAmount());
        assertEquals(new BigDecimal("500.00"), goal.getCurrentAmount());
        assertEquals("HIGH", goal.getPriority());
        assertEquals("PAUSED", goal.getStatus());
        assertEquals("http://test.com", goal.getLink());
        assertFalse(goal.isActive());
    }
}
