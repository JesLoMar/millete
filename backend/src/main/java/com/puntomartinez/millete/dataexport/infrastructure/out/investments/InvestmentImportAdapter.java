package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.InvestmentImportPort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

/** Legacy flat investment records are intentionally not imported into the new ledger. */
@Component
public class InvestmentImportAdapter implements InvestmentImportPort {
    @Override public int importInvestments(List<InvestmentSnapshot> investments, UUID userId) {
        return 0;
    }
}
