package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class InvestmentTrackingApiDTOs {

    private InvestmentTrackingApiDTOs() {
    }

    public record ConfigureInvestmentTrackingRequestDTO(
            @NotNull
            Instant trackingStartAt
    ) {
    }
}