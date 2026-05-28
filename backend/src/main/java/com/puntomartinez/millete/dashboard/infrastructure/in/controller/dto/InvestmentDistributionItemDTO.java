package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;

public record InvestmentDistributionItemDTO(
        String name,
        BigDecimal value,
        double percentage,
        String color
) {}