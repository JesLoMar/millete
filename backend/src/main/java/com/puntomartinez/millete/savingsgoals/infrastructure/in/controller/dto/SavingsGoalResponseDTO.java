package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SavingsGoalResponseDTO(
        UUID id,
        UUID userId,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate deadline,
        String priority,
        String link,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        boolean active
) {
}