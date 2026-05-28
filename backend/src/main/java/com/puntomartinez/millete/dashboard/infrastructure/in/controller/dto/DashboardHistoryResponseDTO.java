package com.puntomartinez.millete.dashboard.infrastructure.in.controller.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardHistoryResponseDTO(
        String period,
        List<String> labels,
        List<BigDecimal> data
) {}