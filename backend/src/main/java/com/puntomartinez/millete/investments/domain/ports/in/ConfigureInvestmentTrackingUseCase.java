package com.puntomartinez.millete.investments.domain.ports.in;

import java.time.Instant;
import java.util.UUID;

public interface ConfigureInvestmentTrackingUseCase {

    void configure(
            UUID userId,
            Instant trackingStartAt
    );
}