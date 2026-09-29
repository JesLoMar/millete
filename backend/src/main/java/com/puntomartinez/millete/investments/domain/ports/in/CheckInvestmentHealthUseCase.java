package com.puntomartinez.millete.investments.domain.ports.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CheckInvestmentHealthUseCase {

    HealthReport check(UUID userId);

    record HealthReport(
            Instant checkedAt,
            List<HealthIssue> issues
    ) {
    }

    record HealthIssue(
            String code,
            String resourceId,
            String severity,
            String message
    ) {
    }
}