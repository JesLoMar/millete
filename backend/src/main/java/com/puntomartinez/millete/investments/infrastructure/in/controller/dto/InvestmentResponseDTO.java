package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvestmentResponseDTO(
        UUID id,
        String assetName,
        String ticker,
        BigDecimal quantity,
        BigDecimal purchasePrice,
        BigDecimal currentPrice,

        // Campos Mágicos (Calculados por el Dominio)
        BigDecimal investedCapital,
        BigDecimal currentValue,
        BigDecimal profitOrLoss,
        BigDecimal roiPercentage,

        InvestmentType type,
        LocalDateTime purchaseDate,
        boolean active
) {}