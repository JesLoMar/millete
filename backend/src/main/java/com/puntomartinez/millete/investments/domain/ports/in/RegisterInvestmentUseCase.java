package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Investment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterInvestmentUseCase {
    Investment register(RegisterInvestmentCommand command);

    record RegisterInvestmentCommand(
            UUID userId,
            String assetName,
            String ticker,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            Investment.InvestmentType type,
            LocalDateTime purchaseDate
    ) {}
}