package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.ports.in.ConfigureInvestmentTrackingUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentTrackingSettingsRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InvestmentTrackingService
        implements ConfigureInvestmentTrackingUseCase {

    private final InvestmentTrackingSettingsRepository trackingSettings;
    private final InvestmentPortfolioLockPort portfolioLock;

    public InvestmentTrackingService(
            InvestmentTrackingSettingsRepository trackingSettings,
            InvestmentPortfolioLockPort portfolioLock
    ) {
        this.trackingSettings = trackingSettings;
        this.portfolioLock = portfolioLock;
    }

    @Override
@Transactional
public void configure(
        UUID userId,
        Instant trackingStartAt
) {
    if (userId == null) {
        throw new InvalidInputException(
                "El usuario es obligatorio."
        );
    }

    if (trackingStartAt == null) {
        throw new InvalidInputException(
                "trackingStartAt es obligatorio."
        );
    }

    portfolioLock.lock(userId);

    if (trackingSettings
            .findTrackingStartAt(userId)
            .isPresent()) {

        throw new InvalidInputException(
                "El tracking de Investments ya está configurado."
        );
    }

    trackingSettings.saveTrackingStartAt(
            userId,
            trackingStartAt
    );
}
}