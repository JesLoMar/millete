package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface WithdrawContributionUseCase {

    void withdrawContribution(
            UUID goalId,
            UUID userId,
            WithdrawContributionCommand command
    );

    record WithdrawContributionCommand(BigDecimal amount) {
    }
}