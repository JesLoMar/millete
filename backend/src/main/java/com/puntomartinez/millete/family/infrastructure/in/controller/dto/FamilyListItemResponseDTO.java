package com.puntomartinez.millete.family.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FamilyListItemResponseDTO(
        UUID id,
        String name,
        BigDecimal monthlyGoal,
        int memberCount,
        boolean isAdmin
) {}