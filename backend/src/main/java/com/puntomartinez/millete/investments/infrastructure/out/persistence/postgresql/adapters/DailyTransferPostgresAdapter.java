package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.ports.out.DailyTransferPort;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Component
public class DailyTransferPostgresAdapter implements DailyTransferPort {
    private final JdbcTemplate jdbc;
    private final UserPreferencesRepository preferences;
    private final TimeProvider time;

    public DailyTransferPostgresAdapter(JdbcTemplate jdbc, UserPreferencesRepository preferences, TimeProvider time) {
        this.jdbc = jdbc; this.preferences = preferences; this.time = time;
    }

    @Override public UUID createTransfer(UUID userId, UUID activityId, TransferDirection direction,
                                         BigDecimal localAmount, String localCurrency,
                                         Instant occurredAt, String description) {
        if (localAmount == null || localAmount.signum() <= 0) throw new IllegalArgumentException("Transfer amount must be positive");
        ZoneId zone = preferences.findByUserId(userId).map(UserPreferences::getPreferences)
                .map(p -> p.get("timezone")).filter(String.class::isInstance).map(String.class::cast)
                .map(this::parseZone).orElse(time.zone());
        UUID id = UUID.randomUUID();
        Instant now = time.now();
        jdbc.update("INSERT INTO transactions(id,user_id,category_id,amount,date,type,description,currency,investment_activity_id,investment_time_zone,created_at,modified_at,active) VALUES(?,?,NULL,?,?,?,?,?,?,?,?,?,true)",
                id, userId, localAmount, occurredAt.atZone(zone).toLocalDate(), direction.name(), description,
                localCurrency, activityId, zone.getId(), java.sql.Timestamp.from(now), java.sql.Timestamp.from(now));
        return id;
    }

    @Override public boolean existsTransfer(UUID transactionId, UUID userId) {
        Boolean exists = jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM transactions WHERE id=? AND user_id=? AND investment_activity_id IS NOT NULL)", Boolean.class, transactionId, userId);
        return Boolean.TRUE.equals(exists);
    }

    @Override public boolean matchesTransfer(UUID userId, UUID activityId, UUID transactionId,
                                             TransferDirection direction, BigDecimal localAmount,
                                             String localCurrency) {
        Boolean matches = jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM transactions WHERE id=? AND user_id=? AND investment_activity_id=? AND type=? AND amount=? AND currency=?)",
                Boolean.class, transactionId, userId, activityId, direction.name(), localAmount, localCurrency);
        return Boolean.TRUE.equals(matches);
    }

    private ZoneId parseZone(String value) {
        try { return ZoneId.of(value); }
        catch (RuntimeException ignored) { return time.zone(); }
    }
}
