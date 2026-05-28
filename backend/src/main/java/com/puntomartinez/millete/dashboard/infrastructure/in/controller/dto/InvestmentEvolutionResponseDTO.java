package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.List;

public record InvestmentEvolutionResponseDTO(
        String period,
        List<String> labels,
        List<BigDecimal> data
) {}