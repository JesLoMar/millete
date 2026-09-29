package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot;

import java.util.UUID;

public interface RestoreInvestmentLedgerUseCase {

    void restore(
            UUID userId,
            InvestmentLedgerSnapshot snapshot
    );
}