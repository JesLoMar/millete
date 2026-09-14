package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.ports.out.InvestmentQueryPort;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InvestmentQueryPostgresAdapter implements InvestmentQueryPort {

    private final InvestmentRepository investmentRepository;

    public InvestmentQueryPostgresAdapter(
            InvestmentRepository investmentRepository
    ) {
        this.investmentRepository = investmentRepository;
    }

    @Override
    public List<InvestmentData> findAllByUserId(UUID userId) {
        return investmentRepository.findAllByUserId(userId)
                .stream()
                .map(this::toInvestmentData)
                .toList();
    }

    private InvestmentData toInvestmentData(Investment investment) {
        return new InvestmentData(
                investment.getId(),
                investment.getQuantity(),
                investment.getPurchasePrice(),
                investment.getCurrentPrice(),
                investment.getType().name(),
                investment.getPurchaseDate(),
                investment.isActive()
        );
    }
}