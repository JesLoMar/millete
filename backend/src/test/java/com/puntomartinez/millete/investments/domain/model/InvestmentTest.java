package com.puntomartinez.millete.investments.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Investment - Modelo de dominio")
class InvestmentTest {

    @Test
    @DisplayName("Constructor por defecto debe crear instancia")
    void defaultConstructor_shouldCreateInstance() {
        Investment inv = new Investment();
        assertNotNull(inv);
    }

    @Test
    @DisplayName("Constructor completo debe asignar todos los valores")
    void fullConstructor_shouldAssignAllValues() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Investment inv = new Investment(
                id, userId, "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, now, now, now, true
        );

        assertEquals(id, inv.getId());
        assertEquals(userId, inv.getUserId());
        assertEquals("Apple", inv.getAssetName());
        assertEquals("AAPL", inv.getTicker());
        assertEquals(new BigDecimal("10"), inv.getQuantity());
        assertEquals(new BigDecimal("150.00"), inv.getPurchasePrice());
        assertEquals(new BigDecimal("180.00"), inv.getCurrentPrice());
        assertEquals(Investment.InvestmentType.STOCK, inv.getType());
        assertTrue(inv.isActive());
    }

    @Test
    @DisplayName("Constructor debe usar purchasePrice como default para currentPrice")
    void fullConstructor_shouldUsePurchasePriceAsDefaultCurrentPrice() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), null,
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        assertEquals(new BigDecimal("150.00"), inv.getCurrentPrice());
    }

    @Test
    @DisplayName("Constructor debe lanzar error con quantity cero")
    void fullConstructor_shouldThrow_whenQuantityZero() {
        assertThrows(IllegalArgumentException.class, () ->
            new Investment(
                    UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                    BigDecimal.ZERO, new BigDecimal("150.00"), new BigDecimal("180.00"),
                    Investment.InvestmentType.STOCK, null, null, null, true
            )
        );
    }

    @Test
    @DisplayName("Constructor debe lanzar error con quantity negativo")
    void fullConstructor_shouldThrow_whenQuantityNegative() {
        assertThrows(IllegalArgumentException.class, () ->
            new Investment(
                    UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                    new BigDecimal("-5"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                    Investment.InvestmentType.STOCK, null, null, null, true
            )
        );
    }

    @Test
    @DisplayName("getInvestedCapital debe calcular correctamente")
    void getInvestedCapital_shouldCalculateCorrectly() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        assertEquals(new BigDecimal("1500.00"), inv.getInvestedCapital());
    }

    @Test
    @DisplayName("getCurrentValue debe calcular correctamente")
    void getCurrentValue_shouldCalculateCorrectly() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        assertEquals(new BigDecimal("1800.00"), inv.getCurrentValue());
    }

    @Test
    @DisplayName("getProfitOrLoss debe calcular ganancia correctamente")
    void getProfitOrLoss_shouldCalculateProfit() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        assertEquals(new BigDecimal("300.00"), inv.getProfitOrLoss());
    }

    @Test
    @DisplayName("getProfitOrLoss debe calcular pérdida correctamente")
    void getProfitOrLoss_shouldCalculateLoss() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("120.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        assertEquals(new BigDecimal("-300.00"), inv.getProfitOrLoss());
    }

    @Test
    @DisplayName("getReturnOnInvestmentPercentage debe calcular correctamente")
    void getReturnOnInvestmentPercentage_shouldCalculateCorrectly() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        BigDecimal roi = inv.getReturnOnInvestmentPercentage();
        assertEquals(0, roi.compareTo(new BigDecimal("20.00")));
    }

    @Test
    @DisplayName("getReturnOnInvestmentPercentage debe devolver cero cuando invested es cero")
    void getReturnOnInvestmentPercentage_shouldReturnZero_whenInvestedZero() {
        Investment inv = new Investment();
        inv.setQuantity(new BigDecimal("10"));
        inv.setPurchasePrice(BigDecimal.ZERO);
        inv.setCurrentPrice(new BigDecimal("180.00"));

        assertEquals(BigDecimal.ZERO, inv.getReturnOnInvestmentPercentage());
    }

    @Test
    @DisplayName("updateCurrentPrice debe actualizar precio")
    void updateCurrentPrice_shouldUpdatePrice() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        inv.updateCurrentPrice(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("200.00"), inv.getCurrentPrice());
        assertNotNull(inv.getModifiedAt());
    }

    @Test
    @DisplayName("updateCurrentPrice debe ignorar precio nulo")
    void updateCurrentPrice_shouldIgnoreNullPrice() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        inv.updateCurrentPrice(null);

        assertEquals(new BigDecimal("180.00"), inv.getCurrentPrice());
    }

    @Test
    @DisplayName("updateCurrentPrice debe ignorar precio negativo")
    void updateCurrentPrice_shouldIgnoreNegativePrice() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        inv.updateCurrentPrice(new BigDecimal("-10.00"));

        assertEquals(new BigDecimal("180.00"), inv.getCurrentPrice());
    }

    @Test
    @DisplayName("deactivate debe marcar como inactivo")
    void deactivate_shouldMarkInactive() {
        Investment inv = new Investment(
                UUID.randomUUID(), UUID.randomUUID(), "Apple", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"), new BigDecimal("180.00"),
                Investment.InvestmentType.STOCK, null, null, null, true
        );

        inv.deactivate();

        assertFalse(inv.isActive());
        assertNotNull(inv.getModifiedAt());
    }

    @Test
    @DisplayName("InvestmentType debe tener valores correctos")
    void investmentType_shouldHaveCorrectValues() {
        assertEquals("STOCK", Investment.InvestmentType.STOCK.name());
        assertEquals("CRYPTO", Investment.InvestmentType.CRYPTO.name());
        assertEquals("FUND", Investment.InvestmentType.FUND.name());
        assertEquals("REAL_ESTATE", Investment.InvestmentType.REAL_ESTATE.name());
        assertEquals("OTHER", Investment.InvestmentType.OTHER.name());
    }
}
