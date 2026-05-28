package com.puntomartinez.millete.transactions.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Transaction - Modelo de dominio")
class TransactionTest {

    private final UUID id = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("Debe crear transacción con datos válidos")
    void shouldCreateTransaction() {
        Transaction tx = new Transaction(id, userId, null, new BigDecimal("50.00"),
                now, Transaction.TransactionType.EXPENSE, "Compra", now, now, true);

        assertThat(tx.getAmount()).isEqualByComparingTo("50.00");
        assertThat(tx.getType()).isEqualTo(Transaction.TransactionType.EXPENSE);
        assertThat(tx.getDescription()).isEqualTo("Compra");
        assertThat(tx.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar error si amount es cero")
    void shouldThrowWhenAmountIsZero() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Transaction(id, userId, null, BigDecimal.ZERO,
                        now, Transaction.TransactionType.EXPENSE, "Compra", now, now, true))
                .withMessage("La cantidad no puede ser cero.");
    }

    @Test
    @DisplayName("Debe lanzar error si amount es nulo")
    void shouldThrowWhenAmountIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Transaction(id, userId, null, null,
                        now, Transaction.TransactionType.EXPENSE, "Compra", now, now, true));
    }

    @Test
    @DisplayName("Debe actualizar detalles correctamente")
    void shouldUpdateDetails() {
        Transaction tx = new Transaction(id, userId, null, new BigDecimal("50.00"),
                now, Transaction.TransactionType.EXPENSE, "Compra", now, now, true);

        UUID categoryId = UUID.randomUUID();
        tx.updateDetails(new BigDecimal("100.00"), now.plusDays(1),
                Transaction.TransactionType.INCOME, "Venta", categoryId);

        assertThat(tx.getAmount()).isEqualByComparingTo("100.00");
        assertThat(tx.getType()).isEqualTo(Transaction.TransactionType.INCOME);
        assertThat(tx.getDescription()).isEqualTo("Venta");
        assertThat(tx.getCategoryId()).isEqualTo(categoryId);
        assertThat(tx.getModifiedAt()).isAfter(now);
    }

    @Test
    @DisplayName("Debe desactivar transacción")
    void shouldDeactivate() {
        Transaction tx = new Transaction(id, userId, null, new BigDecimal("50.00"),
                now, Transaction.TransactionType.EXPENSE, "Compra", now, now, true);
        tx.deactivate();

        assertThat(tx.isActive()).isFalse();
        assertThat(tx.getModifiedAt()).isAfter(now);
    }
}