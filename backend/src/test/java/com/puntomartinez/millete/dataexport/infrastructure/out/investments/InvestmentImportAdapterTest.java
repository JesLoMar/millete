package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentSnapshot;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentImportAdapter")
class InvestmentImportAdapterTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private InvestmentImportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("importInvestments should create new investments with new UUIDs")
    void importInvestmentsShouldCreateNewInvestments() {
        UUID sourceInvestmentId = UUID.randomUUID();
        InvestmentSnapshot snapshot = new InvestmentSnapshot(
                sourceInvestmentId, UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"),
                new BigDecimal("180.00"), "STOCK",
                LocalDate.now().minusMonths(3),
                Instant.now(), Instant.now(), true,
                new BigDecimal("1800.00"), new BigDecimal("300.00"),
                new BigDecimal("20.00")
        );

        when(investmentRepository.save(any(Investment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        int count = adapter.importInvestments(List.of(snapshot), userId);

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).save(captor.capture());

        Investment savedInvestment = captor.getValue();
        assertThat(savedInvestment.getUserId()).isEqualTo(userId);
        assertThat(savedInvestment.getId()).isNotEqualTo(sourceInvestmentId);
        assertThat(savedInvestment.getAssetName()).isEqualTo("Apple");
        assertThat(savedInvestment.getTicker()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("importInvestments should skip inactive investments")
    void importInvestmentsShouldSkipInactiveInvestments() {
        InvestmentSnapshot inactiveSnapshot = new InvestmentSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"),
                new BigDecimal("180.00"), "STOCK",
                LocalDate.now().minusMonths(3),
                Instant.now(), Instant.now(), false,
                new BigDecimal("1800.00"), new BigDecimal("300.00"),
                new BigDecimal("20.00")
        );

        int count = adapter.importInvestments(List.of(inactiveSnapshot), userId);

        assertThat(count).isZero();
        verify(investmentRepository, never()).save(any());
    }
}