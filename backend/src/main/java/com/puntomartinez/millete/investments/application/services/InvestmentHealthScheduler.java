package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.ports.in.CheckInvestmentHealthUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentHealthTargetPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public final class InvestmentHealthScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    InvestmentHealthScheduler.class
            );

    private final InvestmentHealthTargetPort healthTargets;
    private final CheckInvestmentHealthUseCase checkInvestmentHealth;

    public InvestmentHealthScheduler(
            InvestmentHealthTargetPort healthTargets,
            CheckInvestmentHealthUseCase checkInvestmentHealth
    ) {
        this.healthTargets = healthTargets;
        this.checkInvestmentHealth = checkInvestmentHealth;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.investments.health.check-interval-ms:21600000}",
            initialDelayString =
                    "${app.investments.health.initial-delay-ms:120000}"
    )
    public void reconcilePortfolioHealth() {
        for (UUID userId :
                healthTargets.findUserIdsToCheck()) {

            try {
                checkInvestmentHealth.check(
                        userId
                );

            } catch (RuntimeException exception) {
                log.error(
                        "Investment health reconciliation failed "
                                + "for user {}",
                        userId,
                        exception
                );
            }
        }
    }
}