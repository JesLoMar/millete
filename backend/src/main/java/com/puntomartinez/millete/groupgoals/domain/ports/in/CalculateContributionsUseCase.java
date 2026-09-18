package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CalculateContributionsUseCase {

    ContributionsCalculation calculateContributions(
            UUID goalId,
            UUID userId
    );

    record ContributionsCalculation(
            BigDecimal monthlyTarget,
            DistributionMode distributionMode,
            List<MemberContribution> contributions
    ) {
    }

    record MemberContribution(
            UUID userId,
            BigDecimal suggestedAmount
    ) {
    }
}