package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Investment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface UpdateInvestmentUseCase {

    Investment update(UpdateInvestmentCommand command);

    record UpdateInvestmentCommand(
            UUID id,
            UUID userId,
            String assetName,
            String ticker,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            Investment.InvestmentType type,
            LocalDate purchaseDate
    ) {
    }
}
