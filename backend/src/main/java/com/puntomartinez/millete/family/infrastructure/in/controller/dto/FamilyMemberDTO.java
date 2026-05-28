package com.puntomartinez.millete.family.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FamilyMemberDTO(
        UUID id,
        UUID userId,
        String name,
        String role,
        BigDecimal salary,
        BigDecimal customPercentage
) {}