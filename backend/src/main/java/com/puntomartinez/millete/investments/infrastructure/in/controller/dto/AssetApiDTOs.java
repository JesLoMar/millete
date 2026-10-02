package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.AssetOrigin;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AssetApiDTOs {

    private AssetApiDTOs() {
    }

    /*
     * ============================================================
     * SHARED ASSETS
     * ============================================================
     */

    public record SharedAssetResponseDTO(
            UUID id,
            String stableCatalogId,
            String name,
            String symbol,
            AssetType type,
            SectorResponseDTO sector,
            String currency,
            Instant createdAt,
            Instant modifiedAt
    ) {
    }

    /*
     * ============================================================
     * USER ASSETS
     * ============================================================
     */

    public record RegisterUserAssetRequestDTO(
            @NotBlank
            @Size(max = 120)
            String name,

            @NotNull
            AssetType type,

            @NotBlank
            @Size(max = 120)
            String sector,

            @NotNull
            AssetOrigin origin,

            @NotNull
            @DecimalMin("0")
            BigDecimal initialPrice
    ) {
    }

    public record UpdateUserAssetRequestDTO(
            @NotBlank
            @Size(max = 120)
            String name,

            @NotBlank
            @Size(max = 120)
            String sector
    ) {
    }

    public record UserAssetResponseDTO(
            UUID id,
            UUID userId,
            String name,
            AssetType type,
            SectorResponseDTO sector,
            AssetOrigin origin,
            String currency,
            Instant createdAt,
            Instant modifiedAt
    ) {
    }

    /*
     * ============================================================
     * USER ASSET PRICE
     * ============================================================
     */

    public record RegisterUserAssetPriceRequestDTO(
            @NotNull
            @DecimalMin("0")
            BigDecimal unitPrice
    ) {
    }

    public record UserAssetPriceResponseDTO(
            UUID id,
            UUID userAssetId,
            BigDecimal unitPrice,
            String currency,
            Instant timestamp
    ) {
    }

    /*
     * ============================================================
     * SECTORS
     * ============================================================
     */

    public record SectorResponseDTO(
            String code,
            String displayName,
            boolean custom
    ) {
    }

    /*
     * ============================================================
     * ASSET REFERENCE
     * ============================================================
     *
     * Kept here as a small response DTO for consumers that need
     * to identify the kind of asset used by an investment Activity.
     */

    public record AssetReferenceDTO(
            AssetReferenceKind kind,
            UUID id
    ) {
    }

    /*
     * ============================================================
     * SEARCH RESULT
     * ============================================================
     */

    public record SharedAssetSearchResponseDTO(
            List<SharedAssetResponseDTO> assets
    ) {
    }
}