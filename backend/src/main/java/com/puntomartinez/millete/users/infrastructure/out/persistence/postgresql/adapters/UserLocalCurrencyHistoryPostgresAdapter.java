package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.users.domain.ports.out.UserLocalCurrencyHistoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserLocalCurrencyHistoryPostgresAdapter implements UserLocalCurrencyHistoryRepository {
    private final JdbcTemplate jdbc;
    public UserLocalCurrencyHistoryPostgresAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<CurrencyPeriod> findByUserIdAt(UUID userId, Instant at) {
        List<CurrencyPeriod> periods = jdbc.query("SELECT * FROM user_local_currency_history WHERE user_id=? AND valid_from<=? AND (valid_to IS NULL OR valid_to>?) ORDER BY valid_from DESC LIMIT 1",
                (rs,n) -> new CurrencyPeriod((UUID)rs.getObject("id"),(UUID)rs.getObject("user_id"),rs.getString("currency"),rs.getTimestamp("valid_from").toInstant(),rs.getTimestamp("valid_to") == null ? null : rs.getTimestamp("valid_to").toInstant(),rs.getBoolean("inferred")),userId,Timestamp.from(at),Timestamp.from(at));
        return periods.stream().findFirst();
    }

    @Override public Optional<CurrencyPeriod> findOpenByUserId(UUID userId) {
        List<CurrencyPeriod> periods = jdbc.query("SELECT * FROM user_local_currency_history WHERE user_id=? AND valid_to IS NULL",
                (rs,n) -> new CurrencyPeriod((UUID)rs.getObject("id"),(UUID)rs.getObject("user_id"),rs.getString("currency"),rs.getTimestamp("valid_from").toInstant(),null,rs.getBoolean("inferred")),userId);
        return periods.stream().findFirst();
    }

    @Override public void closeOpenPeriod(UUID userId, Instant validTo) {
        jdbc.update("UPDATE user_local_currency_history SET valid_to=? WHERE user_id=? AND valid_to IS NULL",Timestamp.from(validTo),userId);
    }

    @Override public void save(CurrencyPeriod p) {
        jdbc.update("INSERT INTO user_local_currency_history(id,user_id,currency,valid_from,valid_to,inferred) VALUES(?,?,?,?,?,?)",
                p.id(),p.userId(),p.currency(),Timestamp.from(p.validFrom()),p.validTo()==null?null:Timestamp.from(p.validTo()),p.inferred());
    }
}
