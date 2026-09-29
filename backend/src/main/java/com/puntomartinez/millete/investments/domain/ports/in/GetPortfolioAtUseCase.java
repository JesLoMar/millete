package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CalculationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GetPortfolioAtUseCase {

    PortfolioResult get(
            UUID userId,
            Instant asOf
    );

    record PortfolioResult(
            Instant asOf,
            String localCurrency,
            Map<String, BigDecimal> cashBalances,
            List<PositionResult> positions,
            BigDecimal totalInLocalCurrency,
            boolean estimated
    ) {
    }

    record PositionResult(
            AssetReference assetReference,
            BigDecimal quantity,
            BigDecimal costBasis,
            BigDecimal marketValue,
            BigDecimal unrealizedGain,
            String valuationCurrency,
            CalculationStatus calculationStatus,
            boolean estimated,
            boolean historyIncomplete,
            String unavailableReason
    ) {
    }
}