package com.puntomartinez.millete.categories.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegisterCategoryRequestDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(
                max = 20,
                message = "El nombre no puede superar los 20 caracteres"
        )
        String name,

        @Size(
                max = 500,
                message = "La descripción no puede superar los 500 caracteres"
        )
        String description,

        @NotBlank(message = "El color es obligatorio")
        @Pattern(
                regexp = "^#[0-9A-Fa-f]{6}$",
                message = "El color debe ser un hexadecimal válido (ej: #FF5733)"
        )
        String color,

        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "El presupuesto no puede ser negativo"
        )
        BigDecimal budgetLimit
) {
}