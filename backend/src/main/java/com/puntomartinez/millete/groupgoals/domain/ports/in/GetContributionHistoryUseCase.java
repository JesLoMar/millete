package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GetContributionHistoryUseCase {

    ContributionHistory getContributionHistory(
            UUID goalId,
            UUID userId,
            int page,
            int size
    );

    record Contribution(
            UUID id,
            UUID userId,
            String userName,
            BigDecimal amount,
            LocalDateTime date
    ) {
    }

    record ContributionHistory(
            List<Contribution> contributions,
            long totalElements,
            int totalPages
    ) {
    }
}