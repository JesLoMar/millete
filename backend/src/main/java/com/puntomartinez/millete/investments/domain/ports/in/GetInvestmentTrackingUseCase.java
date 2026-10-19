package com.puntomartinez.millete.investments.domain.ports.in;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface GetInvestmentTrackingUseCase {

    Optional<Instant> getTrackingStartAt(UUID userId);
}