package com.puntomartinez.millete.transactions.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction - Modelo de dominio")
class TransactionTest {

    @Test
    @DisplayName("Constructor completo debe asignar todos los valores")
    void fullConstructor_shouldAssignAllValues() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        LocalDateTime now = LocalDateTime.now();

        Transaction tx = new Transaction(
                id, userId, categoryId, new BigDecimal("50.00"),
                date, Transaction.TransactionType.EXPENSE, "Almuerzo",
                now, now, true
        );

        assertEquals(id, tx.getId());
        assertEquals(userId, tx.getUserId());
        assertEquals(categoryId, tx.getCategoryId());
        assertEquals(new BigDecimal("50.00"), tx.getAmount());
        assertEquals(date, tx.getDate());
        assertEquals(Transaction.TransactionType.EXPENSE, tx.getType());
        assertEquals("Almuerzo", tx.getDescription());
        assertTrue(tx.isActive());
    }

    @Test
    @DisplayName("Constructor por defecto debe crear instancia")
    void defaultConstructor_shouldCreateInstance() {
        Transaction tx = new Transaction();

        assertNotNull(tx);
        assertNull(tx.getId());
        assertNull(tx.getAmount());
        assertNull(tx.getDate());
        assertNull(tx.getType());
        assertFalse(tx.isActive());
    }

    @Test
    @DisplayName("updateDetails debe actualizar campos permitidos")
    void updateDetails_shouldUpdateFields() {
        Transaction tx = new Transaction();
        tx.setAmount(new BigDecimal("50.00"));
        tx.setType(Transaction.TransactionType.EXPENSE);
        tx.setDescription("Vieja descripción");

        LocalDateTime newDate = LocalDateTime.now();
        tx.updateDetails(new BigDecimal("100.00"), newDate, Transaction.TransactionType.INCOME, "Nueva descripción", UUID.randomUUID());

        assertEquals(new BigDecimal("100.00"), tx.getAmount());
        assertEquals(newDate, tx.getDate());
        assertEquals(Transaction.TransactionType.INCOME, tx.getType());
        assertEquals("Nueva descripción", tx.getDescription());
    }

    @Test
    @DisplayName("deactivate debe marcar como inactivo")
    void deactivate_shouldMarkInactive() {
        Transaction tx = new Transaction();
        tx.setActive(true);

        tx.deactivate();

        assertFalse(tx.isActive());
    }

    @Test
    @DisplayName("Setters y getters deben funcionar correctamente")
    void settersAndGetters_shouldWork() {
        Transaction tx = new Transaction();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();

        tx.setId(id);
        tx.setUserId(userId);
        tx.setCategoryId(categoryId);
        tx.setAmount(new BigDecimal("75.00"));
        tx.setDate(date);
        tx.setType(Transaction.TransactionType.INCOME);
        tx.setDescription("Salario");
        tx.setActive(false);

        assertEquals(id, tx.getId());
        assertEquals(userId, tx.getUserId());
        assertEquals(categoryId, tx.getCategoryId());
        assertEquals(new BigDecimal("75.00"), tx.getAmount());
        assertEquals(date, tx.getDate());
        assertEquals(Transaction.TransactionType.INCOME, tx.getType());
        assertEquals("Salario", tx.getDescription());
        assertFalse(tx.isActive());
    }

    @Test
    @DisplayName("TransactionType debe tener valores correctos")
    void transactionType_shouldHaveCorrectValues() {
        assertEquals("INCOME", Transaction.TransactionType.INCOME.name());
        assertEquals("EXPENSE", Transaction.TransactionType.EXPENSE.name());
    }
}
