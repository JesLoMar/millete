package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import java.time.Instant;
import java.util.List;

public final class InvestmentHealthApiDTOs {

    private InvestmentHealthApiDTOs() {
    }

    public record HealthResponseDTO(
            Instant checkedAt,
            List<HealthIssueResponseDTO> issues
    ) {
    }

    public record HealthIssueResponseDTO(
            String code,
            String resourceId,
            String severity,
            String message
    ) {
    }
}