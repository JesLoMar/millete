package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Investment;
import java.util.List;
import java.util.UUID;

public interface ListInvestmentsUseCase {
    List<Investment> findAllByUserId(UUID userId);
}