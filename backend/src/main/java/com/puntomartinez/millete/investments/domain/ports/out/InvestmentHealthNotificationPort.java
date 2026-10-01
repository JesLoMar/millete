package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.ports.in.CheckInvestmentHealthUseCase;

import java.util.List;
import java.util.UUID;

public interface InvestmentHealthNotificationPort {

    void reconcile(
            UUID userId,
            List<CheckInvestmentHealthUseCase.HealthIssue> issues
    );
}