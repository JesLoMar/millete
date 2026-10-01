package com.puntomartinez.millete.investments.domain.ports.in;

import java.time.Instant;
import java.util.List;

public interface RefreshMarketDataUseCase {

    void refresh(
            RefreshMarketDataCommand command
    );

    record RefreshMarketDataCommand(
            Instant from,
            Instant to,
            List<String> currencyPairs
    ) {
    }
}