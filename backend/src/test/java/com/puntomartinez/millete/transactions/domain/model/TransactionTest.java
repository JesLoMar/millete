package com.puntomartinez.millete.transactions.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private UUID id = UUID.randomUUID();
    private UUID userId = UUID.randomUUID();
    private UUID categoryId = UUID.randomUUID();
    private LocalDateTime now = LocalDateTime.now();

    @Test
    void constructor_shouldAcceptPositiveAmount() {
        Transaction tx = new Transaction(
                id, userId, categoryId, new BigDecimal("50.00"),
                now, Transaction.TransactionType.EXPENSE, "Almuerzo",
                now, now, true
        );

        assertEquals(new BigDecimal("50.00"), tx.getAmount());
        assertTrue(tx.isActive());
    }

    @Test
    void constructor_shouldRejectNegativeAmount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Transaction(
                        id, userId, categoryId, new BigDecimal("-50.00"),
                        now, Transaction.TransactionType.EXPENSE, "Gasto malicioso",
                        now, now, true
                )
        );

        assertEquals("La cantidad debe ser mayor que cero.", ex.getMessage());
    }

    @Test
    void constructor_shouldRejectZeroAmount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Transaction(
                        id, userId, categoryId, BigDecimal.ZERO,
                        now, Transaction.TransactionType.INCOME, "Ingreso inválido",
                        now, now, true
                )
        );

        assertEquals("La cantidad debe ser mayor que cero.", ex.getMessage());
    }

    @Test
    void updateDetails_shouldRejectNegativeAmount() {
        Transaction tx = new Transaction(
                id, userId, categoryId, new BigDecimal("50.00"),
                now, Transaction.TransactionType.EXPENSE, "Almuerzo",
                now, now, true
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tx.updateDetails(
                        new BigDecimal("-100.00"),
                        now,
                        Transaction.TransactionType.INCOME,
                        "Descripción",
                        categoryId
                )
        );

        assertEquals("La cantidad debe ser mayor que cero.", ex.getMessage());
    }
}
