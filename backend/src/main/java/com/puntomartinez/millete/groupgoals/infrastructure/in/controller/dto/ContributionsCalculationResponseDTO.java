package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ContributionsCalculationResponseDTO(
        BigDecimal monthlyTarget,
        DistributionMode distributionMode,
        List<MemberContributionDTO> contributions
) {
    public record MemberContributionDTO(
            UUID userId,
            BigDecimal suggestedAmount
    ) {
    }
}