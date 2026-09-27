package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases;
import com.puntomartinez.millete.investments.domain.ports.out.AssetRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically reconciles portfolio health so actionable issues reach notifications. */
@Component
public final class InvestmentHealthScheduler {
    private static final Logger log = LoggerFactory.getLogger(InvestmentHealthScheduler.class);

    private final AssetRepository assets;
    private final InvestmentUseCases investments;

    public InvestmentHealthScheduler(AssetRepository assets, InvestmentUseCases investments) {
        this.assets = assets;
        this.investments = investments;
    }

    @Scheduled(fixedDelayString = "${app.investments.health.check-interval-ms:21600000}",
            initialDelayString = "${app.investments.health.initial-delay-ms:120000}")
    public void reconcilePortfolioHealth() {
        for (UUID userId : assets.findUserIdsWithActiveAssets()) {
            try {
                investments.health(userId);
            } catch (RuntimeException exception) {
                log.error("Investment health reconciliation failed for user {}", userId, exception);
            }
        }
    }
}
