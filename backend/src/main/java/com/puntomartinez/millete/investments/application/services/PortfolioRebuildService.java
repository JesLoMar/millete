package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.PortfolioReplay;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.HoldingRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.PortfolioProjectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioRebuildService {

    private final ActivityRepository activities;
    private final HoldingRepository holdings;
    private final PortfolioProjectionRepository projections;
    private final InvestmentPortfolioLockPort portfolioLock;

    public PortfolioRebuildService(
            ActivityRepository activities,
            HoldingRepository holdings,
            PortfolioProjectionRepository projections,
            InvestmentPortfolioLockPort portfolioLock
    ) {
        this.activities = activities;
        this.holdings = holdings;
        this.projections = projections;
        this.portfolioLock = portfolioLock;
    }

    @Transactional
    public void rebuild(UUID userId) {
        portfolioLock.lock(userId);

        List<Activity> userActivities =
                activities.findAllByUserId(userId);

        List<Holding> userHoldings =
                holdings.findAllByUserId(userId);

        Instant trackingStartAt =
                activities
                        .findFirstOccurredAtByUserIdAndType(
                                userId,
                                ActivityType.OPENING_CASH
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe OPENING_CASH para establecer el inicio del tracking"
                                )
                        );

        PortfolioReplay.Result result =
                PortfolioReplay.replay(
                        userId,
                        userActivities,
                        userHoldings,
                        trackingStartAt
                );

        projections.replace(
                userId,
                result.lots(),
                result.consumptions(),
                result.positions()
        );
    }
}