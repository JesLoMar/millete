package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.FxRate;

import java.time.Instant;
import java.util.List;

public interface ListFxRatesUseCase {

    List<FxRate> list(
            String baseCurrency,
            String quoteCurrency,
            Instant from,
            Instant to
    );
}