package com.puntomartinez.millete.family.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FamilyContributionDTO(
        UUID id,
        UUID userId,
        String name,
        BigDecimal amount,
        LocalDateTime date
) {}