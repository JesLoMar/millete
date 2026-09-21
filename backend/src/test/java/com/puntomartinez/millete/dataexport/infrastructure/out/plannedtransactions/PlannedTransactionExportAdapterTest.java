package com.puntomartinez.millete.dataexport.infrastructure.out.plannedtransactions;

import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlannedTransactionExportAdapter")
class PlannedTransactionExportAdapterTest {

    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;

    @InjectMocks
    private PlannedTransactionExportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("findAllByUserId should map planned transactions to snapshots")
    void findAllByUserIdShouldMapPlannedTransactionsToSnapshots() {
        PlannedTransaction ptx = PlannedTransaction.reconstitute(
                UUID.randomUUID(), userId, UUID.randomUUID(),
                new BigDecimal("100.00"),
                Transaction.TransactionType.EXPENSE,
                "Rent",
                PlannedTransaction.FrequencyType.MONTHS,
                1,
                LocalDate.now(),
                null,
                LocalDateTime.now(), LocalDateTime.now(),
                true, null, 0
        );

        when(plannedTransactionRepository.findAllByUserId(userId))
                .thenReturn(List.of(ptx));

        List<PlannedTransactionSnapshot> result = adapter.findAllByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().description()).isEqualTo("Rent");
        assertThat(result.getFirst().amount()).isEqualByComparingTo("100.00");
        assertThat(result.getFirst().type()).isEqualTo("EXPENSE");
        assertThat(result.getFirst().frequencyType()).isEqualTo("MONTHS");
        assertThat(result.getFirst().frequencyInterval()).isEqualTo(1);
        assertThat(result.getFirst().active()).isTrue();
    }

    @Test
    @DisplayName("findAllByUserId should return empty list when no planned transactions")
    void findAllByUserIdShouldReturnEmptyListWhenNoPlannedTransactions() {
        when(plannedTransactionRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<PlannedTransactionSnapshot> result = adapter.findAllByUserId(userId);

        assertThat(result).isEmpty();
    }
}