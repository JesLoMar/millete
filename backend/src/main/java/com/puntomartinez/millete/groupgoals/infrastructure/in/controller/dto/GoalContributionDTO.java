package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoalContributionDTO(
        UUID id,
        UUID userId,
        String userName,
        BigDecimal amount,
        LocalDateTime date
) {}