package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecentTransactionResponseDTO(
        UUID id,
        String description,
        String category,
        String categoryColor,
        UUID categoryId,
        BigDecimal amount,
        LocalDateTime date,
        String type
) {}