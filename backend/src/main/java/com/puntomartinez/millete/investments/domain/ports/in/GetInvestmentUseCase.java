package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Investment;

import java.util.UUID;

public interface GetInvestmentUseCase {

    Investment getById(UUID id, UUID userId);
}