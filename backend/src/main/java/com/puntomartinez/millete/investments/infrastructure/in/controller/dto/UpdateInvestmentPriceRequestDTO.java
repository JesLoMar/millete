package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record UpdateInvestmentPriceRequestDTO(
        @NotNull(message = "El nuevo precio es obligatorio")
        @PositiveOrZero(message = "El precio de mercado no puede ser negativo")
        BigDecimal newPrice
) {}