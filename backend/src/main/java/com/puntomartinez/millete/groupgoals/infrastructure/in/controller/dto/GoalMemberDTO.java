package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalMemberDTO(
        UUID id,
        UUID userId,
        String memberName,
        String role,
        BigDecimal salary,
        BigDecimal customPercentage
) {}