package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.CalculationStatus;
import com.puntomartinez.millete.investments.domain.model.SettlementCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PortfolioApiDTOs {

    private PortfolioApiDTOs() {
    }

    public record CreateHoldingRequestDTO(
            @NotNull
            @Valid
            AssetReferenceRequestDTO assetReference,

            @NotNull
            Instant snapshotAt,

            @NotNull
            @DecimalMin("0.000000000001")
            BigDecimal quantity,

            @NotNull
            @DecimalMin("0")
            BigDecimal acquisitionCost
    ) {
    }

    public record HoldingResponseDTO(
            UUID id,
            UUID userId,
            AssetReferenceResponseDTO assetReference,
            Instant snapshotAt,
            BigDecimal quantity,
            BigDecimal acquisitionCost,
            String currency,
            String status,
            Instant createdAt,
            Instant supersededAt
    ) {
    }

    public record ReplaceHoldingHistoryRequestDTO(
            @NotNull
            @Valid
            @Size(min = 1)
            List<HistoricalActivityDTO> history
    ) {
    }

    public record HistoricalActivityDTO(
            @NotNull
            Instant occurredAt,

            @NotNull
            HistoricalActivityType type,

            BigDecimal quantity,
            BigDecimal unitPrice,
            SettlementCurrency settlementCurrency,

            BigDecimal amount,
            String currency,

            BigDecimal ratio,

            @Size(max = 500)
            String comment
    ) {
    }

    public enum HistoricalActivityType {
        BUY,
        SELL,
        CASH_DIVIDEND,
        IN_KIND_DIVIDEND,
        SPLIT
    }

    public record CashBalancesResponseDTO(
            Map<String, BigDecimal> balances
    ) {
    }

    public record PortfolioResponseDTO(
            Instant asOf,
            String localCurrency,
            Map<String, BigDecimal> cashBalances,
            List<PositionResponseDTO> positions,
            BigDecimal totalInLocalCurrency,
            boolean estimated
    ) {
    }

    public record PositionResponseDTO(
            AssetReferenceResponseDTO assetReference,
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

    public record ClosedLotResponseDTO(
            UUID lotId,
            AssetReferenceResponseDTO assetReference,
            BigDecimal quantity,
            BigDecimal costBasis,
            String costBasisCurrency,
            BigDecimal proceeds,
            String proceedsCurrency,
            BigDecimal realizedGain,
            String realizedGainCurrency,
            Instant openedAt,
            Instant closedAt,
            boolean estimated,
            boolean historyIncomplete
    ) {
    }

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
}