package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface AddContributionUseCase {

    void addContribution(
            UUID goalId,
            UUID userId,
            AddContributionCommand command
    );

    record AddContributionCommand(BigDecimal amount) {
    }
}