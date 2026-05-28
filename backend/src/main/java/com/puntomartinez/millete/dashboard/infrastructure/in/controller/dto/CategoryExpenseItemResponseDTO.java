package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;

public record CategoryExpenseItemResponseDTO(
        String name,
        BigDecimal amount,
        double percentage,
        int transactionCount
) {}