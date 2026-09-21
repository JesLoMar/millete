package com.puntomartinez.millete.dashboard.infrastructure.out.transactions;

import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionQueryPostgresAdapter")
class TransactionQueryPostgresAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionQueryPostgresAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TransactionQueryPostgresAdapter(transactionRepository);
    }

    @Test
    @DisplayName("Should map transactions to TransactionData on findByUserIdAndDateBetween")
    void shouldMapTransactionsOnFindByDateBetween() {
        UUID txId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();

        Transaction tx = Transaction.reconstitute(
                txId, USER_ID, categoryId,
                new BigDecimal("50.00"), date,
                Transaction.TransactionType.EXPENSE,
                "Groceries",
                date, date, true
        );

        when(transactionRepository.findByUserIdAndDateBetween(
                eq(USER_ID), any(), any()
        )).thenReturn(List.of(tx));

        List<TransactionQueryPort.TransactionData> result =
                adapter.findByUserIdAndDateBetween(
                        USER_ID,
                        date.minusDays(30),
                        date
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(txId);
        assertThat(result.get(0).description()).isEqualTo("Groceries");
        assertThat(result.get(0).categoryId()).isEqualTo(categoryId);
        assertThat(result.get(0).amount()).isEqualByComparingTo("50.00");
        assertThat(result.get(0).type()).isEqualTo("EXPENSE");
        verify(transactionRepository).findByUserIdAndDateBetween(
                eq(USER_ID), any(), any()
        );
    }

    @Test
    @DisplayName("Should map transactions to TransactionData on findRecentByUserId")
    void shouldMapTransactionsOnFindRecent() {
        UUID txId = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();

        Transaction tx = Transaction.reconstitute(
                txId, USER_ID, null,
                new BigDecimal("25.00"), date,
                Transaction.TransactionType.INCOME,
                "Salary",
                date, date, true
        );

        when(transactionRepository.findRecentByUserId(USER_ID, 5))
                .thenReturn(List.of(tx));

        List<TransactionQueryPort.TransactionData> result =
                adapter.findRecentByUserId(USER_ID, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(txId);
        assertThat(result.get(0).type()).isEqualTo("INCOME");
        assertThat(result.get(0).categoryId()).isNull();
        verify(transactionRepository).findRecentByUserId(USER_ID, 5);
    }

    @Test
    @DisplayName("Should return empty list when no transactions")
    void shouldReturnEmptyWhenNoTransactions() {
        when(transactionRepository.findRecentByUserId(USER_ID, 5))
                .thenReturn(Collections.emptyList());

        List<TransactionQueryPort.TransactionData> result =
                adapter.findRecentByUserId(USER_ID, 5);

        assertThat(result).isEmpty();
    }
}