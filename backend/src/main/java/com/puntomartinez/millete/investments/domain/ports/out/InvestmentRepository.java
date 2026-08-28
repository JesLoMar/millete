package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository {
    Investment save(Investment investment);
    Optional<Investment> findById(UUID id);
    List<Investment> findAllByUserId(UUID userId);
    List<Investment> findAllByUserId(UUID userId, int page, int size, String search, InvestmentType type);
    long countByUserIdAndFilters(UUID userId, String search, InvestmentType type);
}
