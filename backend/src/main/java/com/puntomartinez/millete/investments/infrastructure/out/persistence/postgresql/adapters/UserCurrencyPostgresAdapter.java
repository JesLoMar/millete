package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.UserLocalCurrencyPeriod;
import com.puntomartinez.millete.investments.domain.ports.out.UserCurrencyPort;
import com.puntomartinez.millete.users.domain.ports.out.UserLocalCurrencyHistoryRepository;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserCurrencyPostgresAdapter implements UserCurrencyPort {
    private final UserPreferencesRepository preferences;
    private final UserLocalCurrencyHistoryRepository history;
    public UserCurrencyPostgresAdapter(UserPreferencesRepository preferences, UserLocalCurrencyHistoryRepository history) {
        this.preferences = preferences; this.history = history;
    }
    @Override public Optional<String> currentCurrency(UUID userId) {
        return preferences.findByUserId(userId).map(p -> p.getPreferences().get("localCurrency"))
                .filter(String.class::isInstance).map(String.class::cast);
    }
    @Override public Optional<UserLocalCurrencyPeriod> currencyAt(UUID userId, Instant at) {
        return history.findByUserIdAt(userId, at).map(p -> new UserLocalCurrencyPeriod(p.id(),p.userId(),p.currency(),p.validFrom(),p.validTo(),p.inferred()));
    }
    @Override public UserLocalCurrencyPeriod savePeriod(UserLocalCurrencyPeriod p) {
        history.save(new UserLocalCurrencyHistoryRepository.CurrencyPeriod(p.id(),p.userId(),p.currency(),p.validFrom(),p.validTo(),p.inferred()));
        return p;
    }
    @Override public void closeCurrentPeriod(UUID userId, Instant validTo) { history.closeOpenPeriod(userId, validTo); }
}
