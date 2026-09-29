package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot;

import java.util.UUID;

public interface GetInvestmentLedgerSnapshotUseCase {

    InvestmentLedgerSnapshot get(UUID userId);
}