package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases.HealthIssue;
import java.util.List;
import java.util.UUID;

/** Synchronizes the current portfolio health issues with user-visible notifications. */
public interface InvestmentHealthNotificationPort {
    void reconcile(UUID userId, List<HealthIssue> issues);
}
