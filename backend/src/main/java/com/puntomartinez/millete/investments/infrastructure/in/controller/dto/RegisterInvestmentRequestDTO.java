package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegisterInvestmentRequestDTO(
        @NotBlank(message = "El nombre del activo es obligatorio")
        String assetName,

        String ticker, // Opcional (ej: BTC, AAPL)

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        BigDecimal quantity,

        @NotNull(message = "El precio de compra es obligatorio")
        @PositiveOrZero(message = "El precio de compra no puede ser negativo")
        BigDecimal purchasePrice,

        @NotNull(message = "El tipo de inversión es obligatorio")
        InvestmentType type,

        @NotNull(message = "La fecha de compra es obligatoria")
        LocalDateTime purchaseDate
) {}