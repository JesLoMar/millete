package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.users.domain.ports.out.UserLocalCurrencyHistoryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public final class UserCurrencyPostgresAdapter
        implements UserCurrencyPort {

    private final UserLocalCurrencyHistoryRepository history;

    public UserCurrencyPostgresAdapter(
            UserLocalCurrencyHistoryRepository history
    ) {
        this.history = history;
    }

    @Override
    public Optional<CurrencyCode> currencyAt(
            UUID userId,
            Instant at
    ) {
        return history
                .findByUserIdAt(
                        userId,
                        at
                )
                .map(period ->
                        CurrencyCode.of(
                                period.currency()
                        )
                );
    }
}