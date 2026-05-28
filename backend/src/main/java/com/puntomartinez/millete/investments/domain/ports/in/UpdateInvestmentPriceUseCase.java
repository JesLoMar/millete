package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Investment;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateInvestmentPriceUseCase {
    Investment updatePrice(UUID id, UUID userId, BigDecimal newPrice);
}