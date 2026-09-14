package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentEvolutionResponseDTO;

import java.util.UUID;

public interface GetInvestmentEvolutionUseCase {

    InvestmentEvolutionResponseDTO getInvestmentEvolution(
            UUID userId,
            String period
    );
}