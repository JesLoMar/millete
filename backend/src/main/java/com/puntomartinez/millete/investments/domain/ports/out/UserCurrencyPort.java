package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.UserLocalCurrencyPeriod;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserCurrencyPort {
    Optional<String> currentCurrency(UUID userId);
    Optional<UserLocalCurrencyPeriod> currencyAt(UUID userId, Instant at);
    UserLocalCurrencyPeriod savePeriod(UserLocalCurrencyPeriod period);
    void closeCurrentPeriod(UUID userId, Instant validTo);
}
