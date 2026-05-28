package com.puntomartinez.millete.family.domain.ports.in;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface CalculateFamilyContributionsUseCase {
    Map<UUID, BigDecimal> calculateContributions(UUID familyId);
}