package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetItemResponseDTO(
        UUID categoryId,
        String category,
        BigDecimal spent,
        BigDecimal limit,
        double percentage
) {}