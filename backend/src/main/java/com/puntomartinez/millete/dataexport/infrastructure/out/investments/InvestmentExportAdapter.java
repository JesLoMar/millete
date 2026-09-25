package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentExportPort;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InvestmentExportAdapter implements InvestmentExportPort {

    private final InvestmentRepository investmentRepository;

    public InvestmentExportAdapter(
            InvestmentRepository investmentRepository
    ) {
        this.investmentRepository = investmentRepository;
    }

    @Override
    public List<InvestmentSnapshot> findAllByUserId(UUID userId) {
        return investmentRepository.findAllByUserId(userId)
                .stream()
                .map(investment ->
                        new InvestmentSnapshot(
                                investment.getId(),
                                investment.getUserId(),
                                investment.getAssetName(),
                                investment.getTicker(),
                                investment.getQuantity(),
                                investment.getPurchasePrice(),
                                investment.getCurrentPrice(),
                                investment.getType().name(),
                                investment.getPurchaseDate(),
                                investment.getCreatedAt(),
                                investment.getModifiedAt(),
                                investment.isActive(),
                                investment.getCurrentValue(),
                                investment.getProfitOrLoss(),
                                investment.getReturnOnInvestmentPercentage()
                        )
                )
                .toList();
    }
}