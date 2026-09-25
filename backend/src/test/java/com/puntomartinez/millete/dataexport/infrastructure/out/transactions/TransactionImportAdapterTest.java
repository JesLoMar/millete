package com.puntomartinez.millete.dataexport.infrastructure.out.transactions;

import com.puntomartinez.millete.dataexport.domain.model.TransactionSnapshot;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionImportAdapter")
class TransactionImportAdapterTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionImportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("importTransactions should remap category IDs and user IDs")
    void importTransactionsShouldRemapCategoryIdsAndUserIds() {
        UUID sourceCategoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();
        UUID sourceTxId = UUID.randomUUID();

        TransactionSnapshot snapshot = new TransactionSnapshot(
                sourceTxId, UUID.randomUUID(), sourceCategoryId,
                new BigDecimal("50.00"), Instant.now(),
                "EXPENSE", "Lunch",
                Instant.now(), Instant.now(), true
        );

        Map<UUID, UUID> categoryIdMap = new HashMap<>();
        categoryIdMap.put(sourceCategoryId, newCategoryId);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        int count = adapter.importTransactions(
                List.of(snapshot), userId, categoryIdMap
        );

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction savedTx = captor.getValue();
        assertThat(savedTx.getUserId()).isEqualTo(userId);
        assertThat(savedTx.getId()).isNotEqualTo(sourceTxId);
        assertThat(savedTx.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(savedTx.getDescription()).isEqualTo("Lunch");
    }

    @Test
    @DisplayName("importTransactions should skip inactive transactions")
    void importTransactionsShouldSkipInactiveTransactions() {
        TransactionSnapshot inactiveSnapshot = new TransactionSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("50.00"), Instant.now(),
                "EXPENSE", "Inactive",
                Instant.now(), Instant.now(), false
        );

        int count = adapter.importTransactions(
                List.of(inactiveSnapshot), userId, new HashMap<>()
        );

        assertThat(count).isZero();
        verify(transactionRepository, never()).save(any());
    }
}