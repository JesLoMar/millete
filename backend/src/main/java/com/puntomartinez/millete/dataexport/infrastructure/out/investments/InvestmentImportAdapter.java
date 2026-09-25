package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentImportPort;
import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InvestmentImportAdapter
        implements InvestmentImportPort {

    private final InvestmentRepository investmentRepository;

    public InvestmentImportAdapter(
            InvestmentRepository investmentRepository
    ) {
        this.investmentRepository = investmentRepository;
    }

    @Override
    public int importInvestments(
            List<InvestmentSnapshot> investments,
            UUID userId) {

        if (investments == null || investments.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (InvestmentSnapshot investment : investments) {

            if (!investment.active()) {
                continue;
            }

            Investment importedInvestment =
                    Investment.reconstitute(
                            UUID.randomUUID(),
                            userId,
                            investment.assetName(),
                            investment.ticker(),
                            investment.quantity(),
                            investment.purchasePrice(),
                            investment.currentPrice(),
                            Investment.InvestmentType.valueOf(
                                    investment.type()
                            ),
                            investment.purchaseDate(),
                            investment.createdAt(),
                            investment.modifiedAt(),
                            investment.active()
                    );

            investmentRepository.save(
                    importedInvestment
            );

            count++;
        }

        return count;
    }
}