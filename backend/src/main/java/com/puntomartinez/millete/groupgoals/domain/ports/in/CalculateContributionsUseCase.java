package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface CalculateContributionsUseCase {
    Map<UUID, BigDecimal> calculateContributions(UUID goalId, UUID callerId);
}