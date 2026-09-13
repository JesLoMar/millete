package com.puntomartinez.millete.investments.domain.ports.in;

import java.util.UUID;

public interface DeleteInvestmentUseCase {

    void delete(UUID id, UUID userId);
}