package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GoalContributionDTO(
        UUID id,
        UUID userId,
        String userName,
        BigDecimal amount,
        Instant date
) {}