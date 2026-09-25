package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterInvestmentRequestDTO(

        @NotBlank(message = "El nombre del activo es obligatorio")
        @Size(
                max = 100,
                message = "El nombre del activo no puede superar los 100 caracteres"
        )
        String assetName,

        @Size(
                max = 20,
                message = "El ticker no puede superar los 20 caracteres"
        )
        String ticker,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        BigDecimal quantity,

        @NotNull(message = "El precio de compra es obligatorio")
        @Positive(message = "El precio de compra debe ser mayor a cero")
        BigDecimal purchasePrice,

        @NotNull(message = "El tipo de inversión es obligatorio")
        InvestmentType type,

        @NotNull(message = "La fecha de compra es obligatoria")
        LocalDate purchaseDate
) {
}
