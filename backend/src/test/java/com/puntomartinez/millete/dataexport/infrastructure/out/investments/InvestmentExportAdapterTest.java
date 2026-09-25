package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentSnapshot;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentExportAdapter")
class InvestmentExportAdapterTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private InvestmentExportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("findAllByUserId should map investments to snapshots")
    void findAllByUserIdShouldMapInvestmentsToSnapshots() {
        Investment inv = Investment.reconstitute(
                UUID.randomUUID(), userId, "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"),
                new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK,
                LocalDate.now().minusMonths(3),
                Instant.now(), Instant.now(), true
        );

        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of(inv));

        List<InvestmentSnapshot> result = adapter.findAllByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().assetName()).isEqualTo("Apple");
        assertThat(result.getFirst().ticker()).isEqualTo("AAPL");
        assertThat(result.getFirst().quantity()).isEqualByComparingTo("10");
        assertThat(result.getFirst().purchasePrice()).isEqualByComparingTo("150.00");
        assertThat(result.getFirst().currentPrice()).isEqualByComparingTo("180.00");
        assertThat(result.getFirst().type()).isEqualTo("STOCK");
        assertThat(result.getFirst().active()).isTrue();
        assertThat(result.getFirst().currentValue()).isEqualByComparingTo("1800.00");
        assertThat(result.getFirst().profitOrLoss()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("findAllByUserId should return empty list when no investments")
    void findAllByUserIdShouldReturnEmptyListWhenNoInvestments() {
        when(investmentRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<InvestmentSnapshot> result = adapter.findAllByUserId(userId);

        assertThat(result).isEmpty();
    }
}