package com.puntomartinez.millete.investments.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Investment - Modelo de dominio")
class InvestmentTest {

    private final UUID id = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("Debe calcular investedCapital correctamente")
    void shouldCalculateInvestedCapital() {
        Investment inv = new Investment(id, userId, "Nvidia", "NVDA",
                new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("150.00"),
                Investment.InvestmentType.STOCK, now, now, now, true);

        assertThat(inv.getInvestedCapital()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Debe calcular currentValue correctamente")
    void shouldCalculateCurrentValue() {
        Investment inv = new Investment(id, userId, "Nvidia", "NVDA",
                new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("150.00"),
                Investment.InvestmentType.STOCK, now, now, now, true);

        assertThat(inv.getCurrentValue()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Debe calcular profitOrLoss correctamente")
    void shouldCalculateProfit() {
        Investment inv = new Investment(id, userId, "Nvidia", "NVDA",
                new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("150.00"),
                Investment.InvestmentType.STOCK, now, now, now, true);

        assertThat(inv.getProfitOrLoss()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Debe calcular ROI positivo")
    void shouldCalculatePositiveROI() {
        Investment inv = new Investment(id, userId, "Nvidia", "NVDA",
                new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("150.00"),
                Investment.InvestmentType.STOCK, now, now, now, true);

        assertThat(inv.getReturnOnInvestmentPercentage()).isEqualByComparingTo("50.0000");
    }

    @Test
    @DisplayName("Debe calcular pérdida")
    void shouldCalculateLoss() {
        Investment inv = new Investment(id, userId, "Nvidia", "NVDA",
                new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("80.00"),
                Investment.InvestmentType.STOCK, now, now, now, true);

        assertThat(inv.getProfitOrLoss()).isEqualByComparingTo("-200.00");
    }

    @Test
    @DisplayName("Debe lanzar error si cantidad es cero")
    void shouldThrowWhenQuantityIsZero() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Investment(id, userId, "Nvidia", "NVDA",
                        BigDecimal.ZERO, new BigDecimal("100.00"), new BigDecimal("150.00"),
                        Investment.InvestmentType.STOCK, now, now, now, true));
    }

    @Test
    @DisplayName("Debe actualizar precio correctamente")
    void shouldUpdatePrice() {
        Investment inv = new Investment(id, userId, "Nvidia", "NVDA",
                new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("150.00"),
                Investment.InvestmentType.STOCK, now, now, now, true);

        inv.updateCurrentPrice(new BigDecimal("200.00"));

        assertThat(inv.getCurrentPrice()).isEqualByComparingTo("200.00");
        assertThat(inv.getCurrentValue()).isEqualByComparingTo("2000.00");
        assertThat(inv.getModifiedAt()).isAfterOrEqualTo(now);
    }

    @Test
    @DisplayName("Debe usar purchasePrice como currentPrice si es nulo")
    void shouldUsePurchasePriceAsCurrent() {
        Investment inv = new Investment(id, userId, "Nvidia", "NVDA",
                new BigDecimal("10"), new BigDecimal("100.00"), null,
                Investment.InvestmentType.STOCK, now, now, now, true);

        assertThat(inv.getCurrentPrice()).isEqualByComparingTo("100.00");
    }
}