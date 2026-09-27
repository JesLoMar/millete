package com.puntomartinez.millete.users.domain.ports.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserLocalCurrencyHistoryRepository {
    void lockForUpdate(UUID userId);
    Optional<CurrencyPeriod> findByUserIdAt(UUID userId, Instant at);
    Optional<CurrencyPeriod> findOpenByUserId(UUID userId);
    Optional<CurrencyPeriod> findLatestByUserId(UUID userId);
    void closeOpenPeriod(UUID userId, Instant validTo);
    void save(CurrencyPeriod period);
    java.util.List<CurrencyPeriod> findAllByUserId(UUID userId);
    void deleteAllByUserId(UUID userId);

    record CurrencyPeriod(UUID id, UUID userId, String currency,
                          Instant validFrom, Instant validTo, boolean inferred) { }
}
