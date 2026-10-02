package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.PerformanceAttribution;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.PerformanceApiDTOs;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PerformanceResponseMapper {

    public PerformanceApiDTOs.PerformanceResponseDTO toResponse(
            PerformanceAttribution performance
    ) {
        if (performance == null) {
            return null;
        }

        return new PerformanceApiDTOs.PerformanceResponseDTO(
                performance.getFrom(),
                performance.getTo(),
                performance.getCurrency().value(),
                amount(performance.getOpeningValue()),
                amount(performance.getEndingValue()),
                performance.portfolioChange(),
                amount(performance.getOpeningCapital()),
                amount(performance.getContributions()),
                amount(performance.getWithdrawals()),
                amount(performance.getRealizedGains()),
                amount(performance.getUnrealizedPriceEffect()),
                amount(performance.getFxEffect()),
                amount(performance.getCashDividends()),
                amount(performance.getInKindDividends()),
                amount(performance.getReconciliationDifference()),
                performance.isEstimated(),
                performance.getStatus(),
                performance.getUnavailableReason()
        );
    }

    private BigDecimal amount(
            Money money
    ) {
        return money == null
                ? null
                : money.amount();
    }
}