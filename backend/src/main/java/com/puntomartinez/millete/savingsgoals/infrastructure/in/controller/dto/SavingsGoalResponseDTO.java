package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto;

import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record SavingsGoalResponseDTO(
        UUID id,
        UUID userId,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate deadline,
        GoalPriority priority,
        String link,
        Instant createdAt,
        Instant modifiedAt,
        boolean active
) {}
