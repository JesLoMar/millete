package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.SettlementCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ActivityApiDTOs {

    private ActivityApiDTOs() {
    }

    /*
     * ============================================================
     * ASSET REFERENCE
     * ============================================================
     */

    public record AssetReferenceRequestDTO(
            @NotNull AssetReferenceKind kind,
            @NotNull UUID id
    ) {
    }

    public record AssetReferenceResponseDTO(
            AssetReferenceKind kind,
            UUID id
    ) {
    }

    /*
     * ============================================================
     * BUY
     * ============================================================
     */

    public record BuyActivityRequestDTO(
            Instant occurredAt,

            @NotNull
            @Valid
            AssetReferenceRequestDTO assetReference,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal quantity,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal unitPrice,

            @NotNull
            SettlementCurrency settlementCurrency,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * SELL
     * ============================================================
     */

    public record SellActivityRequestDTO(
            Instant occurredAt,

            @NotNull
            @Valid
            AssetReferenceRequestDTO assetReference,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal quantity,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal unitPrice,

            @NotNull
            SettlementCurrency settlementCurrency,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * CASH DIVIDEND
     * ============================================================
     */

    public record CashDividendActivityRequestDTO(
            Instant occurredAt,

            @Valid
            AssetReferenceRequestDTO assetReference,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal amount,

            @NotBlank
            @Pattern(regexp = "[A-Za-z]{3}")
            String currency,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * IN-KIND DIVIDEND
     * ============================================================
     */

    public record InKindDividendActivityRequestDTO(
            Instant occurredAt,

            @NotNull
            @Valid
            AssetReferenceRequestDTO assetReference,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal quantity,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * DEPOSIT
     * ============================================================
     */

    public record DepositActivityRequestDTO(
            Instant occurredAt,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal amount,

            @NotBlank
            @Pattern(regexp = "[A-Za-z]{3}")
            String currency,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * WITHDRAWAL
     * ============================================================
     */

    public record WithdrawalActivityRequestDTO(
            Instant occurredAt,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal amount,

            @NotBlank
            @Pattern(regexp = "[A-Za-z]{3}")
            String currency,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * EXCHANGE
     * ============================================================
     */

    public record ExchangeActivityRequestDTO(
            Instant occurredAt,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal amountOrigin,

            @NotBlank
            @Pattern(regexp = "[A-Za-z]{3}")
            String currencyOrigin,

            @NotBlank
            @Pattern(regexp = "[A-Za-z]{3}")
            String currencyDestination,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * SPLIT
     * ============================================================
     */

    public record SplitActivityRequestDTO(
            Instant occurredAt,

            @NotNull
            @Valid
            AssetReferenceRequestDTO assetReference,

            @NotNull
            @DecimalMin("0.0000000000000001")
            BigDecimal ratio,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * EDIT ACTIVITY
     * ============================================================
     */

    public record EditActivityRequestDTO(
            @NotNull Instant occurredAt,

            @NotNull
            @Valid
            EditActivityDetailsDTO details,

            @NotBlank
            @Size(max = 500)
            String reason
    ) {
    }

    public enum EditActivityDetailsType {
        BUY_SELL,
        CASH_DIVIDEND,
        IN_KIND_DIVIDEND,
        EXCHANGE,
        SPLIT
    }

    public record EditActivityDetailsDTO(
            @NotNull EditActivityDetailsType type,

            @DecimalMin("0.000000000001")
            BigDecimal quantity,

            @DecimalMin("0.000000000001")
            BigDecimal unitPrice,

            SettlementCurrency settlementCurrency,

            @DecimalMin("0.000000000001")
            BigDecimal amount,

            @DecimalMin("0.000000000001")
            BigDecimal amountOrigin,

            @DecimalMin("0.0000000000000001")
            BigDecimal ratio,

            @Size(max = 500)
            String comment
    ) {
    }

    /*
     * ============================================================
     * MONEY
     * ============================================================
     */

    public record MoneyResponseDTO(
            BigDecimal amount,
            String currency
    ) {
    }

    /*
     * ============================================================
     * FX
     * ============================================================
     */

    public record AppliedFxRateResponseDTO(
            String baseCurrency,
            String quoteCurrency,
            BigDecimal rate,
            String source,
            Instant timestamp
    ) {
    }

    /*
     * ============================================================
     * ACTIVITY RESPONSE
     * ============================================================
     */

    public record ActivityResponseDTO(
            UUID id,
            ActivityType type,
            AssetReferenceResponseDTO assetReference,
            Instant occurredAt,
            Instant createdAt,
            Instant modifiedAt,
            long orderingKey,
            ActivityDetailsResponseDTO details,
            String comment,
            UUID linkedTransactionId
    ) {
    }

    public record ActivityDetailsResponseDTO(
            TradeActivityResponseDTO trade,
            CashActivityResponseDTO cash,
            SplitActivityResponseDTO split,
            ExchangeActivityResponseDTO exchange,
            InKindDividendActivityResponseDTO inKindDividend,
            OpeningPositionActivityResponseDTO openingPosition
    ) {
    }

    public record TradeActivityResponseDTO(
            BigDecimal quantity,
            MoneyResponseDTO unitPrice,
            MoneyResponseDTO settlementAmount,
            AppliedFxRateResponseDTO appliedFxRate
    ) {
    }

    public record CashActivityResponseDTO(
            MoneyResponseDTO amount
    ) {
    }

    public record SplitActivityResponseDTO(
            BigDecimal ratio
    ) {
    }

    public record ExchangeActivityResponseDTO(
            MoneyResponseDTO origin,
            MoneyResponseDTO destination,
            AppliedFxRateResponseDTO appliedFxRate
    ) {
    }

    public record InKindDividendActivityResponseDTO(
            BigDecimal quantity,
            MoneyResponseDTO referenceUnitPrice
    ) {
    }

    public record OpeningPositionActivityResponseDTO(
            BigDecimal quantity,
            MoneyResponseDTO acquisitionCost
    ) {
    }

    /*
     * ============================================================
     * AUDIT
     * ============================================================
     */

    public record ActivityAuditResponseDTO(
            UUID id,
            UUID activityId,
            String beforeJson,
            String afterJson,
            String reason,
            Instant changedAt
    ) {
    }
}