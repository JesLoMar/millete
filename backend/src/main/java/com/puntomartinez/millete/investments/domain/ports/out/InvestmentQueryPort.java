package com.puntomartinez.millete.investments.domain.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvestmentQueryPort {

    List<InvestmentData> findAllByUserId(UUID userId);

    record InvestmentData(
            UUID id,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            BigDecimal currentPrice,
            String type,
            LocalDate purchaseDate,
            boolean active
    ) {

        public BigDecimal getInvestedCapital() {
            return quantity.multiply(purchasePrice);
        }

        public BigDecimal getCurrentValue() {
            return quantity.multiply(currentPrice);
        }

        public BigDecimal getProfitOrLoss() {
            return getCurrentValue().subtract(getInvestedCapital());
        }
    }
}
