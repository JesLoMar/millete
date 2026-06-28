package com.puntomartinez.millete.plannedtransactions.domain.model;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlannedTransaction - Modelo de dominio")
class PlannedTransactionTest {

    @Test
    @DisplayName("Constructor por defecto debe crear instancia")
    void defaultConstructor_shouldCreateInstance() {
        PlannedTransaction ptx = new PlannedTransaction();

        assertNotNull(ptx);
        assertNull(ptx.getId());
    }

    @Test
    @DisplayName("Constructor completo debe asignar todos los valores")
    void fullConstructor_shouldAssignAllValues() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusMonths(1);
        LocalDateTime now = LocalDateTime.now();

        PlannedTransaction ptx = new PlannedTransaction(
                id, userId, categoryId, new BigDecimal("100.00"),
                Transaction.TransactionType.EXPENSE, "Alquiler",
                PlannedTransaction.FrequencyType.MONTHS, 1,
                startDate, endDate, now, now, true, startDate
        );

        assertEquals(id, ptx.getId());
        assertEquals(userId, ptx.getUserId());
        assertEquals(categoryId, ptx.getCategoryId());
        assertEquals(new BigDecimal("100.00"), ptx.getAmount());
        assertEquals(Transaction.TransactionType.EXPENSE, ptx.getType());
        assertEquals("Alquiler", ptx.getDescription());
        assertEquals(PlannedTransaction.FrequencyType.MONTHS, ptx.getFrequencyType());
        assertEquals(1, ptx.getFrequencyInterval());
        assertEquals(startDate, ptx.getStartDate());
        assertEquals(endDate, ptx.getEndDate());
        assertTrue(ptx.isActive());
        assertEquals(startDate, ptx.getLastExecutedDate());
    }

    @Test
    @DisplayName("Constructor debe lanzar error con amount cero")
    void fullConstructor_shouldThrow_whenAmountZero() {
        assertThrows(IllegalArgumentException.class, () ->
            new PlannedTransaction(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO,
                    Transaction.TransactionType.EXPENSE, "Test",
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null, null, null, true, null
            )
        );
    }

    @Test
    @DisplayName("Constructor debe lanzar error con endDate anterior a startDate")
    void fullConstructor_shouldThrow_whenEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class, () ->
            new PlannedTransaction(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"),
                    Transaction.TransactionType.EXPENSE, "Test",
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), LocalDate.now().minusDays(1), null, null, true, null
            )
        );
    }

    @Test
    @DisplayName("Setters y getters deben funcionar correctamente")
    void settersAndGetters_shouldWork() {
        PlannedTransaction ptx = new PlannedTransaction();
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        ptx.setId(id);
        ptx.setAmount(new BigDecimal("200.00"));
        ptx.setType(Transaction.TransactionType.INCOME);
        ptx.setDescription("Salario");
        ptx.setFrequencyType(PlannedTransaction.FrequencyType.MONTHS);
        ptx.setFrequencyInterval(2);
        ptx.setStartDate(date);
        ptx.setEndDate(date.plusMonths(6));
        ptx.setActive(false);
        ptx.setLastExecutedDate(date);
        ptx.setCreatedAt(now);
        ptx.setModifiedAt(now);

        assertEquals(id, ptx.getId());
        assertEquals(new BigDecimal("200.00"), ptx.getAmount());
        assertEquals(Transaction.TransactionType.INCOME, ptx.getType());
        assertEquals("Salario", ptx.getDescription());
        assertEquals(PlannedTransaction.FrequencyType.MONTHS, ptx.getFrequencyType());
        assertEquals(2, ptx.getFrequencyInterval());
        assertEquals(date, ptx.getStartDate());
        assertEquals(date.plusMonths(6), ptx.getEndDate());
        assertFalse(ptx.isActive());
        assertEquals(date, ptx.getLastExecutedDate());
    }

    @Test
    @DisplayName("FrequencyType debe tener valores correctos")
    void frequencyType_shouldHaveCorrectValues() {
        assertEquals("DAYS", PlannedTransaction.FrequencyType.DAYS.name());
        assertEquals("WEEKS", PlannedTransaction.FrequencyType.WEEKS.name());
        assertEquals("MONTHS", PlannedTransaction.FrequencyType.MONTHS.name());
        assertEquals("YEARS", PlannedTransaction.FrequencyType.YEARS.name());
    }
}
