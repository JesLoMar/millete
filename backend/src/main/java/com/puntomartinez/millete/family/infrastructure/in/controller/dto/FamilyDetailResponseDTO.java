package com.puntomartinez.millete.family.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record FamilyDetailResponseDTO(
        UUID id,
        String name,
        BigDecimal monthlyGoal,
        String distributionMode,
        boolean isAdmin,
        List<FamilyMemberDTO> members,
        List<FamilyContributionDTO> contributions
) {}