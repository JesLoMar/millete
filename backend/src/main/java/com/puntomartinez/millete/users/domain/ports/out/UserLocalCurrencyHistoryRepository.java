package com.puntomartinez.millete.users.domain.ports.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserLocalCurrencyHistoryRepository {
    Optional<CurrencyPeriod> findByUserIdAt(UUID userId, Instant at);
    Optional<CurrencyPeriod> findOpenByUserId(UUID userId);
    void closeOpenPeriod(UUID userId, Instant validTo);
    void save(CurrencyPeriod period);

    record CurrencyPeriod(UUID id, UUID userId, String currency,
                          Instant validFrom, Instant validTo, boolean inferred) { }
}
