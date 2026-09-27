package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Public request and response shapes for the investments API. */
public final class InvestmentApiDTOs {
    private InvestmentApiDTOs() { }

    public record BuyActivityRequestDTO(@NotNull Instant occurredAt, @NotNull UUID assetId,
            @NotNull @DecimalMin("0.000000000001") BigDecimal quantity,
            @NotNull @DecimalMin("0.000000000001") BigDecimal unitPrice,
            @DecimalMin("0.000000000001") BigDecimal amount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency, @Size(max=500) String comment) { }
    public record SellActivityRequestDTO(@NotNull Instant occurredAt, @NotNull UUID assetId,
            @NotNull @DecimalMin("0.000000000001") BigDecimal quantity,
            @NotNull @DecimalMin("0.000000000001") BigDecimal unitPrice,
            @DecimalMin("0.000000000001") BigDecimal amount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency, @Size(max=500) String comment) { }
    public record DividendActivityRequestDTO(@NotNull Instant occurredAt, @NotNull UUID assetId,
            @NotNull @DecimalMin("0.000000000001") BigDecimal amount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency, @Size(max=500) String comment) { }
    public record InterestActivityRequestDTO(@NotNull Instant occurredAt, @NotNull UUID assetId,
            @NotNull @DecimalMin("0.000000000001") BigDecimal amount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency, @Size(max=500) String comment) { }
    public record DepositActivityRequestDTO(@NotNull Instant occurredAt,
            @NotNull @DecimalMin("0.000000000001") BigDecimal amount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency, @Size(max=500) String comment) { }
    public record WithdrawActivityRequestDTO(@NotNull Instant occurredAt,
            @NotNull @DecimalMin("0.000000000001") BigDecimal amount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency, @Size(max=500) String comment) { }
    public record OpeningCashActivityRequestDTO(@NotNull Instant occurredAt,
            @NotNull @DecimalMin("0.000000000001") BigDecimal amount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency, @Size(max=500) String comment) { }
    public record SplitActivityRequestDTO(@NotNull Instant occurredAt, @NotNull UUID assetId,
            @NotNull @DecimalMin("0.0000000000000001") BigDecimal ratio, @Size(max=500) String comment) { }
    public record ExchangeActivityRequestDTO(@NotNull Instant occurredAt,
            @NotNull @DecimalMin("0.000000000001") BigDecimal amount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency,
            @NotNull @DecimalMin("0.000000000001") BigDecimal secondaryAmount,
            @NotBlank @Pattern(regexp="[A-Za-z]{3}") String secondaryCurrency,
            @NotNull @DecimalMin("0.0000000000000001") BigDecimal exchangeRate,
            @Size(max=500) String comment) { }
    public record EditActivityRequestDTO(@NotNull Instant occurredAt,
            @DecimalMin("0.000000000001") BigDecimal quantity,
            @DecimalMin("0.000000000001") BigDecimal unitPrice,
            @DecimalMin("0.000000000001") BigDecimal amount,
            @DecimalMin("0.0000000000000001") BigDecimal ratio,
            @Size(max=500) String comment, @NotBlank @Size(max=500) String reason) { }
    public record CommentRequestDTO(@Size(max=500) String comment) { }

    public record SectorResponseDTO(UUID id, String code, String displayName) { }
    public record HoldingResponseDTO(UUID id, UUID assetId, Instant snapshotAt, BigDecimal quantity,
            BigDecimal acquisitionCost, String currency, boolean historyIncomplete,
            boolean superseded, Instant createdAt) { }
    public record CashBalanceResponseDTO(String currency, BigDecimal balance) { }
    public record PositionResponseDTO(UUID assetId, String assetName, String symbol, String assetCurrency,
            BigDecimal quantity, BigDecimal costBasis, BigDecimal price, BigDecimal marketValue,
            BigDecimal unrealizedGain, String valuationCurrency, BigDecimal fxRateToLocal,
            Instant priceTimestamp, String priceSource, Instant fxTimestamp, String fxSource,
            boolean estimated, boolean historyIncomplete, InvestmentUseCases.ValuationStatus valuationStatus,
            String unavailableReason) { }
    public record CashValuationResponseDTO(String currency, BigDecimal balance, BigDecimal fxRateToLocal,
            BigDecimal amountInLocalCurrency, Instant fxTimestamp, String fxSource,
            InvestmentUseCases.ValuationStatus valuationStatus) { }
    public record PortfolioResponseDTO(Instant asOf, Map<String, BigDecimal> cashBalances,
            List<PositionResponseDTO> positions, BigDecimal totalInLocalCurrency, String localCurrency,
            boolean estimated, InvestmentUseCases.ValuationStatus valuationStatus,
            boolean historyIncomplete, List<CashValuationResponseDTO> cashValuations) { }
    /** Response item for one fully consumed FIFO lot. */
    public record ClosedLotResponseDTO(UUID lotId, UUID assetId, String assetName, String symbol, String currency,
            BigDecimal quantity, BigDecimal costBasis, BigDecimal proceeds, BigDecimal realizedGain,
            Instant openedAt, Instant closedAt, boolean estimated, boolean historyIncomplete) { }
    public record AssetPriceResponseDTO(UUID id, UUID assetId, Instant timestamp, BigDecimal open,
            BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal adjustedClose,
            BigDecimal valuationPrice, BigDecimal volume, String currency, String source,
            Instant fetchedAt) { }
    public record FxRateResponseDTO(UUID id, String baseCurrency, String quoteCurrency, Instant timestamp,
            BigDecimal rate, String source, Instant fetchedAt) { }
    public record PerformanceResponseDTO(Instant from, Instant to, String currency,
            BigDecimal openingValue, BigDecimal endingValue, BigDecimal portfolioChange,
            BigDecimal contributions, BigDecimal withdrawals, BigDecimal netExternalFlows,
            BigDecimal realizedGains, BigDecimal unrealizedGainChange, BigDecimal dividends,
            BigDecimal interest, BigDecimal fxEffect, BigDecimal openingCashAdjustments,
            BigDecimal exchangeAdjustments, BigDecimal reconciliationDifference,
            boolean estimated, boolean dataIncomplete, String status, String unavailableReason) { }
    public record HealthIssueResponseDTO(String key, String code, String resourceId,
            String severity, String message) { }
    public record ActivityAuditResponseDTO(UUID id, UUID activityId, String beforeJson,
            String afterJson, String reason, Instant changedAt) { }
    public record HealthCenterResponseDTO(Instant checkedAt, List<HealthIssueResponseDTO> issues) { }
    public record RefreshResponseDTO(int pricesStored, int fxRatesStored, String provider) { }
}
