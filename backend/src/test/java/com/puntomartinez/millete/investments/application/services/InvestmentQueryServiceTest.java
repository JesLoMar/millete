package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.ports.out.InvestmentQueryPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentQueryPort.InvestmentData;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentDistributionResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentEvolutionResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentMetricsResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentQueryService")
class InvestmentQueryServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private InvestmentQueryPort investmentQueryPort;

    @InjectMocks
    private InvestmentQueryService investmentQueryService;

    private InvestmentData stockInvestment() {
        return new InvestmentData(
                UUID.randomUUID(),
                new BigDecimal("10"),
                new BigDecimal("150.00"),
                new BigDecimal("180.00"),
                "STOCK",
                LocalDateTime.now().minusDays(30),
                true
        );
    }

    private InvestmentData cryptoInvestment() {
        return new InvestmentData(
                UUID.randomUUID(),
                new BigDecimal("0.5"),
                new BigDecimal("40000.00"),
                new BigDecimal("45000.00"),
                "CRYPTO",
                LocalDateTime.now().minusDays(60),
                true
        );
    }

    @Nested
    @DisplayName("getInvestmentMetrics")
    class GetInvestmentMetrics {

        @Test
        @DisplayName("Should calculate portfolio metrics for month period")
        void shouldCalculatePortfolioMetrics() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(stockInvestment()));

            InvestmentMetricsResponseDTO result =
                    investmentQueryService.getInvestmentMetrics(USER_ID, "month");

            assertThat(result).isNotNull();
            assertThat(result.portfolioValue()).isNotNull();
            assertThat(result.monthlyReturn()).isNotNull();
        }

        @Test
        @DisplayName("Should return zero metrics when no investments")
        void shouldReturnZeroMetricsWhenNoInvestments() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            InvestmentMetricsResponseDTO result =
                    investmentQueryService.getInvestmentMetrics(USER_ID, "month");

            assertThat(result.portfolioValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.monthlyReturn()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle week period")
        void shouldHandleWeekPeriod() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(stockInvestment()));

            InvestmentMetricsResponseDTO result =
                    investmentQueryService.getInvestmentMetrics(USER_ID, "week");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle year period")
        void shouldHandleYearPeriod() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(stockInvestment()));

            InvestmentMetricsResponseDTO result =
                    investmentQueryService.getInvestmentMetrics(USER_ID, "year");

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getInvestmentEvolution")
    class GetInvestmentEvolution {

        @Test
        @DisplayName("Should return weekly evolution with data points up to today")
        void shouldReturnWeeklyEvolution() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(stockInvestment()));

            InvestmentEvolutionResponseDTO result =
                    investmentQueryService.getInvestmentEvolution(USER_ID, "week");

            assertThat(result.period()).isEqualTo("week");
            // El servicio solo incluye días hasta hoy (break en día futuro)
            int dayOfWeek = LocalDate.now().getDayOfWeek().getValue(); // 1=Lunes, 7=Domingo
            assertThat(result.labels()).hasSize(dayOfWeek);
            assertThat(result.data()).hasSize(dayOfWeek);
        }

        @Test
        @DisplayName("Should return monthly evolution with 6 data points")
        void shouldReturnMonthlyEvolution() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(stockInvestment()));

            InvestmentEvolutionResponseDTO result =
                    investmentQueryService.getInvestmentEvolution(USER_ID, "month");

            assertThat(result.period()).isEqualTo("month");
            assertThat(result.labels()).hasSize(6);
            assertThat(result.data()).hasSize(6);
        }

        @Test
        @DisplayName("Should return yearly evolution up to current month")
        void shouldReturnYearlyEvolution() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(stockInvestment()));

            InvestmentEvolutionResponseDTO result =
                    investmentQueryService.getInvestmentEvolution(USER_ID, "year");

            assertThat(result.period()).isEqualTo("year");
            assertThat(result.labels()).isNotEmpty();
            assertThat(result.data()).hasSameSizeAs(result.labels());
        }

        @Test
        @DisplayName("Should return empty evolution when no investments")
        void shouldReturnEmptyEvolutionWhenNoInvestments() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            InvestmentEvolutionResponseDTO result =
                    investmentQueryService.getInvestmentEvolution(USER_ID, "month");

            assertThat(result.data()).allSatisfy(
                    value -> assertThat(value).isEqualByComparingTo(BigDecimal.ZERO)
            );
        }

        @Test
        @DisplayName("Should throw for invalid period")
        void shouldThrowForInvalidPeriod() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(stockInvestment()));

            assertThatThrownBy(() ->
                    investmentQueryService.getInvestmentEvolution(USER_ID, "invalid")
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getInvestmentDistribution")
    class GetInvestmentDistribution {

        @Test
        @DisplayName("Should return distribution grouped by type")
        void shouldReturnDistributionGroupedByType() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(List.of(stockInvestment(), cryptoInvestment()));

            InvestmentDistributionResponseDTO result =
                    investmentQueryService.getInvestmentDistribution(USER_ID, "month");

            assertThat(result.totalValue()).isNotNull();
            assertThat(result.distribution()).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty distribution when no investments")
        void shouldReturnEmptyDistributionWhenNoInvestments() {
            when(investmentQueryPort.findAllByUserId(USER_ID))
                    .thenReturn(Collections.emptyList());

            InvestmentDistributionResponseDTO result =
                    investmentQueryService.getInvestmentDistribution(USER_ID, "month");

            assertThat(result.totalValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.distribution()).isEmpty();
        }
    }
}