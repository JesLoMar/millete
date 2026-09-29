package com.puntomartinez.millete.investments.domain.ports.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface GetInvestmentCashUseCase {

    Map<String, BigDecimal> get(UUID userId, Instant asOf);
}