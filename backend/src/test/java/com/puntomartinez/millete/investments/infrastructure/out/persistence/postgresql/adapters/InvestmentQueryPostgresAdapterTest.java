package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentQueryPort.InvestmentData;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentQueryPostgresAdapter")
class InvestmentQueryPostgresAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private InvestmentQueryPostgresAdapter adapter;

    @Test
    @DisplayName("Should map investments to InvestmentData")
    void shouldMapInvestmentsToInvestmentData() {
        Investment investment = Investment.create(
                USER_ID, "Apple Inc.", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"),
                InvestmentType.STOCK, Instant.now().minusDays(30)
        );

        when(investmentRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(investment));

        List<InvestmentData> result = adapter.findAllByUserId(USER_ID);

        assertThat(result).hasSize(1);
        InvestmentData data = result.getFirst();
        assertThat(data.id()).isEqualTo(investment.getId());
        assertThat(data.quantity()).isEqualByComparingTo("10");
        assertThat(data.purchasePrice()).isEqualByComparingTo("150.00");
        assertThat(data.currentPrice()).isEqualByComparingTo("150.00");
        assertThat(data.type()).isEqualTo("STOCK");
        assertThat(data.active()).isTrue();
    }

    @Test
    @DisplayName("Should return empty list when no investments")
    void shouldReturnEmptyListWhenNoInvestments() {
        when(investmentRepository.findAllByUserId(USER_ID))
                .thenReturn(Collections.emptyList());

        List<InvestmentData> result = adapter.findAllByUserId(USER_ID);

        assertThat(result).isEmpty();
    }
}