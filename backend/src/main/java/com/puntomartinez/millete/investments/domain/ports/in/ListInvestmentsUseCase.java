package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import java.util.List;
import java.util.UUID;

public interface ListInvestmentsUseCase {
    List<Investment> findAllByUserId(UUID userId);
    List<Investment> findAllByUserId(UUID userId, int page, int size, String search, InvestmentType type);
    long countByUserIdAndFilters(UUID userId, String search, InvestmentType type);
}
