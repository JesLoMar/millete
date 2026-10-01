package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.UserLocalCurrencyPeriod;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.users.domain.ports.out.UserLocalCurrencyHistoryRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserCurrencyPostgresAdapter
        implements UserCurrencyPort {

    private final UserLocalCurrencyHistoryRepository history;

    public UserCurrencyPostgresAdapter(
            UserLocalCurrencyHistoryRepository history
    ) {
        this.history = history;
    }

    @Override
    public Optional<UserLocalCurrencyPeriod> currencyAt(
            UUID userId,
            Instant at
    ) {
        return history
                .findByUserIdAt(
                        userId,
                        at
                )
                .map(period ->
                        new UserLocalCurrencyPeriod(
                                period.id(),
                                period.userId(),
                                period.currency(),
                                period.validFrom(),
                                period.validTo(),
                                period.inferred()
                        )
                );
    }
}