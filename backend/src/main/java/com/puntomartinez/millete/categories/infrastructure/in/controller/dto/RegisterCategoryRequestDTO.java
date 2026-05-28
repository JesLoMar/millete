package com.puntomartinez.millete.categories.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record RegisterCategoryRequestDTO(
        @NotBlank(message = "El nombre no puede estar vacío")
        String name,

        @NotBlank(message = "El color no puede estar vacío")
        String color,

        BigDecimal budgetLimit
) {}