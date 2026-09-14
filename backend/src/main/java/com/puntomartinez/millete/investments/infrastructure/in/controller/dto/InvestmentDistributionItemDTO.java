package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import java.math.BigDecimal;

public record InvestmentDistributionItemDTO(
        String name,
        BigDecimal value,
        double percentage,
        String color
) {}
