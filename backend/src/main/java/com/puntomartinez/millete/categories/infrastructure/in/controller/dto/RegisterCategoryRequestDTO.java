package com.puntomartinez.millete.categories.infrastructure.in.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RegisterCategoryRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 1, max = 50, message = "El nombre debe tener entre 1 y 50 caracteres")
        String name,

        @NotBlank(message = "El color es obligatorio")
        @Pattern(
                regexp = "^#[0-9A-Fa-f]{6}$",
                message = "El color debe ser un hexadecimal válido (ej: #FF5733)"
        )
        String color,

        @DecimalMin(value = "0.0", inclusive = false, message = "El presupuesto debe ser mayor que 0")
        BigDecimal budgetLimit
) {}