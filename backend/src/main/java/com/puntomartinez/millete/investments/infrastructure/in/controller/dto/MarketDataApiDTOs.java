package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class MarketDataApiDTOs {

    private MarketDataApiDTOs() {
    }

    /*
     * ============================================================
     * ASSET PRICE
     * ============================================================
     */

    public record AssetPriceResponseDTO(
            UUID id,
            UUID sharedAssetId,
            Instant timestamp,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal adjustedClose,
            BigDecimal valuationPrice,
            BigDecimal volume,
            String currency,
            String source,
            Instant fetchedAt
    ) {
    }

    /*
     * ============================================================
     * FX RATE
     * ============================================================
     */

    public record FxRateResponseDTO(
            UUID id,
            String baseCurrency,
            String quoteCurrency,
            Instant timestamp,
            BigDecimal rate,
            String source,
            Instant fetchedAt
    ) {
    }

    /*
     * ============================================================
     * MARKET DATA REFRESH
     * ============================================================
     */

    public record RefreshMarketDataRequestDTO(
            @NotNull
            Instant from,

            @NotNull
            Instant to,

            @NotNull
            @Size(max = 100)
            List<
                    @NotBlank
                    @Size(max = 20)
                    String
                    > currencyPairs
    ) {
    }
}