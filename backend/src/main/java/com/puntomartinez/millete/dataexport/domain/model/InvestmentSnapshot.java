package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvestmentSnapshot(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("userId")
        UUID userId,

        @JsonProperty("assetName")
        String assetName,

        @JsonProperty("ticker")
        String ticker,

        @JsonProperty("quantity")
        BigDecimal quantity,

        @JsonProperty("purchasePrice")
        BigDecimal purchasePrice,

        @JsonProperty("currentPrice")
        BigDecimal currentPrice,

        @JsonProperty("type")
        String type,

        @JsonProperty("purchaseDate")
        LocalDate purchaseDate,

        @JsonProperty("createdAt")
        LocalDateTime createdAt,

        @JsonProperty("modifiedAt")
        LocalDateTime modifiedAt,

        @JsonProperty("active")
        boolean active,

        @JsonProperty("currentValue")
        BigDecimal currentValue,

        @JsonProperty("profitOrLoss")
        BigDecimal profitOrLoss,

        @JsonProperty("returnOnInvestmentPercentage")
        BigDecimal returnOnInvestmentPercentage
) {
}