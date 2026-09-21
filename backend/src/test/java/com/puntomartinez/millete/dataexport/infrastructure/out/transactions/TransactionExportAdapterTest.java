package com.puntomartinez.millete.dataexport.infrastructure.out.transactions;

import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionExportAdapter")
class TransactionExportAdapterTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionExportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("findAllByUserId should map transactions to snapshots")
    void findAllByUserIdShouldMapTransactionsToSnapshots() {
        Transaction tx = Transaction.reconstitute(
                UUID.randomUUID(), userId, UUID.randomUUID(),
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Lunch",
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(tx));

        List<TransactionSnapshot> result = adapter.findAllByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().description()).isEqualTo("Lunch");
        assertThat(result.getFirst().amount()).isEqualByComparingTo("50.00");
        assertThat(result.getFirst().type()).isEqualTo("EXPENSE");
        assertThat(result.getFirst().active()).isTrue();
    }

    @Test
    @DisplayName("findByUserIdAndDateBetween should filter by date range")
    void findByUserIdAndDateBetweenShouldFilterByDateRange() {
        Transaction tx = Transaction.reconstitute(
                UUID.randomUUID(), userId, UUID.randomUUID(),
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Lunch",
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        when(transactionRepository.findByUserIdAndDateBetween(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(tx));

        List<TransactionSnapshot> result =
                adapter.findByUserIdAndDateBetween(userId, start, end);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findAllByUserId should return empty list when no transactions")
    void findAllByUserIdShouldReturnEmptyListWhenNoTransactions() {
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<TransactionSnapshot> result = adapter.findAllByUserId(userId);

        assertThat(result).isEmpty();
    }
}