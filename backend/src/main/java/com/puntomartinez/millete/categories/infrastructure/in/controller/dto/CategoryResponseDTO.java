package com.puntomartinez.millete.categories.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        UUID userId,
        String name,
        String color,
        BigDecimal budgetLimit,
        Instant createdAt,
        boolean active
) {
}
