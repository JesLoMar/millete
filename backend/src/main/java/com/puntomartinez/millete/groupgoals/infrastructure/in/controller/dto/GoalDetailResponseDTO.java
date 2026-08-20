package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GoalDetailResponseDTO(
        UUID id,
        String name,
        BigDecimal monthlyTarget,
        String distributionMode,
        boolean isAdmin,
        List<GoalMemberDTO> members,
        List<GoalContributionDTO> contributions,
        Map<UUID, BigDecimal> contributionTotals
) {}
