package com.puntomartinez.millete.categories.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        UUID userId,
        String name,
        String color,
        BigDecimal budgetLimit,
        LocalDateTime createdAt,
        boolean active
) {}