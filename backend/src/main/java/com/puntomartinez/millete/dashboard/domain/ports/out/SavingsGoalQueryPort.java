package com.puntomartinez.millete.dashboard.domain.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SavingsGoalQueryPort {

    List<SavingsGoalData> findAllByUserId(UUID userId);

    record SavingsGoalData(
            UUID id,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDate deadline,
            String priority,
            Instant createdAt
    ) {
    }
}
