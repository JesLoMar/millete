package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.CalculationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public final class PerformanceApiDTOs {

    private PerformanceApiDTOs() {
    }

    public record PerformanceResponseDTO(
            Instant from,
            Instant to,
            String currency,
            BigDecimal openingValue,
            BigDecimal endingValue,
            BigDecimal portfolioChange,
            BigDecimal openingCapital,
            BigDecimal contributions,
            BigDecimal withdrawals,
            BigDecimal realizedGains,
            BigDecimal unrealizedPriceEffect,
            BigDecimal fxEffect,
            BigDecimal cashDividends,
            BigDecimal inKindDividends,
            BigDecimal reconciliationDifference,
            boolean estimated,
            CalculationStatus status,
            String unavailableReason
    ) {
    }
}