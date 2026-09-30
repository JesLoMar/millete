package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.PortfolioReplay;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.domain.ports.out.HoldingRepository;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentTrackingSettingsRepository;
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
    private final InvestmentTrackingSettingsRepository trackingSettings;

    public PortfolioRebuildService(
            ActivityRepository activities,
            HoldingRepository holdings,
            PortfolioProjectionRepository projections,
            InvestmentPortfolioLockPort portfolioLock,
            InvestmentTrackingSettingsRepository trackingSettings
    ) {
        this.activities = activities;
        this.holdings = holdings;
        this.projections = projections;
        this.portfolioLock = portfolioLock;
        this.trackingSettings = trackingSettings;
    }

    @Transactional
    public void rebuild(UUID userId) {
        portfolioLock.lock(userId);

        List<Activity> userActivities =
                activities.findAllByUserId(userId);

        List<Holding> userHoldings =
                holdings.findAllByUserId(userId);

        Instant trackingStartAt =
                trackingSettings
                        .findTrackingStartAt(userId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El tracking de Investments no está configurado."
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