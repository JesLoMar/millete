package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalListItemResponseDTO(
        UUID id,
        String name,
        BigDecimal monthlyTarget,
        long activeMembers,
        boolean isAdmin
) {}
