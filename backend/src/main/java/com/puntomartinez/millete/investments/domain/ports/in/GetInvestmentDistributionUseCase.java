package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentDistributionResponseDTO;

import java.util.UUID;

public interface GetInvestmentDistributionUseCase {

    InvestmentDistributionResponseDTO getInvestmentDistribution(
            UUID userId,
            String period
    );
}