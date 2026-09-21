package com.puntomartinez.millete.dataexport.infrastructure.out.plannedtransactions;

import com.puntomartinez.millete.dataexport.domain.model.PlannedTransactionSnapshot;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlannedTransactionImportAdapter")
class PlannedTransactionImportAdapterTest {

    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;

    @InjectMocks
    private PlannedTransactionImportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("importPlannedTransactions should remap category IDs")
    void importPlannedTransactionsShouldRemapCategoryIds() {
        UUID sourceCategoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();
        UUID sourcePlannedTxId = UUID.randomUUID();

        PlannedTransactionSnapshot snapshot = new PlannedTransactionSnapshot(
                sourcePlannedTxId, UUID.randomUUID(), sourceCategoryId,
                new BigDecimal("100.00"), "EXPENSE", "Rent",
                "MONTHS", 1, LocalDate.now(), null,
                LocalDateTime.now(), LocalDateTime.now(), true, null
        );

        Map<UUID, UUID> categoryIdMap = new HashMap<>();
        categoryIdMap.put(sourceCategoryId, newCategoryId);

        when(plannedTransactionRepository.save(any(PlannedTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        int count = adapter.importPlannedTransactions(
                List.of(snapshot), userId, categoryIdMap
        );

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<PlannedTransaction> captor =
                ArgumentCaptor.forClass(PlannedTransaction.class);
        verify(plannedTransactionRepository).save(captor.capture());

        PlannedTransaction savedPtx = captor.getValue();
        assertThat(savedPtx.getUserId()).isEqualTo(userId);
        assertThat(savedPtx.getId()).isNotEqualTo(sourcePlannedTxId);
        assertThat(savedPtx.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(savedPtx.getDescription()).isEqualTo("Rent");
    }

    @Test
    @DisplayName("importPlannedTransactions should skip inactive planned transactions")
    void importPlannedTransactionsShouldSkipInactivePlannedTransactions() {
        PlannedTransactionSnapshot inactiveSnapshot = new PlannedTransactionSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("100.00"), "EXPENSE", "Rent",
                "MONTHS", 1, LocalDate.now(), null,
                LocalDateTime.now(), LocalDateTime.now(), false, null
        );

        int count = adapter.importPlannedTransactions(
                List.of(inactiveSnapshot), userId, new HashMap<>()
        );

        assertThat(count).isZero();
        verify(plannedTransactionRepository, never()).save(any());
    }
}