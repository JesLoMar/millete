package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AddAssetPriceRequestDTO(@NotNull UUID assetId, @NotNull Instant timestamp,
                                     BigDecimal open, BigDecimal high, BigDecimal low,
                                     BigDecimal close, BigDecimal adjustedClose,
                                     BigDecimal volume,
                                     @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency,
                                     @NotBlank @Size(max=100) String source) { }
