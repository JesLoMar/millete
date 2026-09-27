package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshot;

import java.util.List;
import java.util.UUID;

public interface InvestmentImportPort {

    int importInvestments(
            InvestmentLedgerSnapshot investments,
            UUID userId
    );
}
