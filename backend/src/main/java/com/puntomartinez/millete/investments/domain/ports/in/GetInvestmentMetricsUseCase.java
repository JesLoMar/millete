package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentMetricsResponseDTO;

import java.util.UUID;

public interface GetInvestmentMetricsUseCase {

    InvestmentMetricsResponseDTO getInvestmentMetrics(
            UUID userId,
            String period
    );
}