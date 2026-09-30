package com.puntomartinez.millete.investments.domain.ports.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentTrackingSettingsRepository {

    Optional<Instant> findTrackingStartAt(UUID userId);

    void saveTrackingStartAt(
            UUID userId,
            Instant trackingStartAt
    );
}