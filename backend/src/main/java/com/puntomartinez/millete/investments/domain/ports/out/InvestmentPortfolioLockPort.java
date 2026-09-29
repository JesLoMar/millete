package com.puntomartinez.millete.investments.domain.ports.out;

import java.util.UUID;

public interface InvestmentPortfolioLockPort {

    void lock(UUID userId);
}