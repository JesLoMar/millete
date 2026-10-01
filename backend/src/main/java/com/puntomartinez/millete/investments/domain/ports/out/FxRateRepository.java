package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FxRateRepository {

    void saveAll(
            List<FxRate> rates
    );

    Optional<FxRate> findLatestAt(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency,
            Instant at
    );

    List<FxRate> findByCurrenciesAndTimestampBetween(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency,
            Instant from,
            Instant to
    );
}