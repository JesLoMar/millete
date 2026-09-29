package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.PerformanceAttribution;

import java.time.Instant;
import java.util.UUID;

public interface CalculatePerformanceUseCase {

    PerformanceAttribution calculate(
            UUID userId,
            Instant from,
            Instant to
    );
}