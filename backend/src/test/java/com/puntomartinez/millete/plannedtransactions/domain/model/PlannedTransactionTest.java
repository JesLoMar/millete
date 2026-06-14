package com.puntomartinez.millete.plannedtransactions.domain.model;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("PlannedTransaction - Modelo de dominio")
class PlannedTransactionTest {

    private final UUID id = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDate today = LocalDate.now();

    @Test
    @DisplayName("Debe crear transacción programada con datos válidos")
    void shouldCreatePlannedTransaction() {
        PlannedTransaction tx = new PlannedTransaction(id, userId, null,
                new BigDecimal("100.00"), Transaction.TransactionType.EXPENSE,
                "Suscripción", PlannedTransaction.FrequencyType.MONTHS, 1,
                today, null, now, now, true, 
                null);

        assertThat(tx.getFrequencyType()).isEqualTo(PlannedTransaction.FrequencyType.MONTHS);
        assertThat(tx.getFrequencyInterval()).isEqualTo(1);
        assertThat(tx.getEndDate()).isNull();
        assertThat(tx.getLastExecutedDate()).isNull();
    }

    @Test
    @DisplayName("Debe crear transacción programada y asignar correctamente lastExecutedDate")
    void shouldAssignLastExecutedDateCorrectly() {
        LocalDate lastExecuted = today.minusDays(5);
        
        PlannedTransaction tx = new PlannedTransaction(id, userId, null,
                new BigDecimal("100.00"), Transaction.TransactionType.EXPENSE,
                "Suscripción", PlannedTransaction.FrequencyType.MONTHS, 1,
                today, null, now, now, true, 
                lastExecuted);

        assertThat(tx.getLastExecutedDate()).isEqualTo(lastExecuted);
    }

    @Test
    @DisplayName("Debe lanzar error si amount es cero")
    void shouldThrowWhenAmountIsZero() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PlannedTransaction(id, userId, null, BigDecimal.ZERO,
                        Transaction.TransactionType.EXPENSE, "Suscripción",
                        PlannedTransaction.FrequencyType.MONTHS, 1,
                        today, null, now, now, true, 
                        null));
    }

    @Test
    @DisplayName("Debe lanzar error si endDate es anterior a startDate")
    void shouldThrowWhenEndDateBeforeStartDate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PlannedTransaction(id, userId, null, new BigDecimal("100.00"),
                        Transaction.TransactionType.EXPENSE, "Suscripción",
                        PlannedTransaction.FrequencyType.MONTHS, 1,
                        today, today.minusDays(1), now, now, true, 
                        null))
                .withMessage("La fecha de fin no puede ser anterior a la fecha de inicio.");
    }

    @Test
    @DisplayName("Debe permitir endDate igual a startDate")
    void shouldAllowEndDateEqualToStartDate() {
        PlannedTransaction tx = new PlannedTransaction(id, userId, null,
                new BigDecimal("100.00"), Transaction.TransactionType.EXPENSE,
                "Suscripción", PlannedTransaction.FrequencyType.MONTHS, 1,
                today, today, now, now, true, 
                null);

        assertThat(tx.getEndDate()).isEqualTo(today);
    }
}