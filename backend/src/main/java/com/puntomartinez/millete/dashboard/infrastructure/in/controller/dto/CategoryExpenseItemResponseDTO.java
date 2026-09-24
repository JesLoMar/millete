package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryExpenseItemResponseDTO(
        UUID categoryId,
        String name,
        BigDecimal amount,
        double percentage,
        int transactionCount

) {}