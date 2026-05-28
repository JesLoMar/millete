package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SavingsGoalResponseDTO(
        UUID id,
        String name,
        BigDecimal current,
        BigDecimal target,
        double percentage,
        String icon,
        LocalDate deadline
) {}