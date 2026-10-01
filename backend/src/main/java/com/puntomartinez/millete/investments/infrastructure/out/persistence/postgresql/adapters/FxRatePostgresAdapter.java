package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public final class FxRatePostgresAdapter
        implements FxRateRepository {

    private final JdbcTemplate jdbc;

    public FxRatePostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public void saveAll(
            List<FxRate> rates
    ) {
        if (rates == null
                || rates.isEmpty()) {
            return;
        }

        try {
            jdbc.batchUpdate(
                    """
                    INSERT INTO fx_rates (
                        id,
                        base_currency,
                        quote_currency,
                        rate_timestamp,
                        rate,
                        source,
                        fetched_at
                    )
                    VALUES (
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?
                    )
                    ON CONFLICT (
                        base_currency,
                        quote_currency,
                        rate_timestamp,
                        source
                    )
                    DO UPDATE SET
                        rate = EXCLUDED.rate,
                        fetched_at = EXCLUDED.fetched_at
                    """,
                    rates,
                    rates.size(),
                    (
                            PreparedStatement statement,
                            FxRate rate
                    ) -> {
                        statement.setObject(
                                1,
                                rate.id()
                        );

                        statement.setString(
                                2,
                                rate.baseCurrency().value()
                        );

                        statement.setString(
                                3,
                                rate.quoteCurrency().value()
                        );

                        statement.setTimestamp(
                                4,
                                timestamp(
                                        rate.timestamp()
                                )
                        );

                        statement.setBigDecimal(
                                5,
                                rate.rate()
                        );

                        statement.setString(
                                6,
                                rate.source()
                        );

                        statement.setTimestamp(
                                7,
                                timestamp(
                                        rate.fetchedAt()
                                )
                        );
                    }
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudieron guardar los tipos de cambio.",
                    exception
            );
        }
    }

    @Override
    public Optional<FxRate> findLatestAt(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency,
            Instant at
    ) {
        validateCurrencies(
                baseCurrency,
                quoteCurrency
        );

        if (at == null) {
            return Optional.empty();
        }

        try {
            return jdbc.query(
                    """
                    SELECT
                        id,
                        base_currency,
                        quote_currency,
                        rate_timestamp,
                        rate,
                        source,
                        fetched_at
                    FROM fx_rates
                    WHERE base_currency = ?
                      AND quote_currency = ?
                      AND rate_timestamp <= ?
                    ORDER BY
                        rate_timestamp DESC,
                        fetched_at DESC,
                        source,
                        id DESC
                    LIMIT 1
                    """,
                    fxRateMapper(),
                    baseCurrency.value(),
                    quoteCurrency.value(),
                    timestamp(at)
            )
            .stream()
            .findFirst();

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo consultar el último tipo de cambio.",
                    exception
            );
        }
    }

    @Override
    public List<FxRate> findByCurrenciesAndTimestampBetween(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency,
            Instant from,
            Instant to
    ) {
        validateCurrencies(
                baseCurrency,
                quoteCurrency
        );

        if (from == null
                || to == null
                || from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "El rango temporal de FX no es válido."
            );
        }

        try {
            return jdbc.query(
                    """
                    SELECT
                        id,
                        base_currency,
                        quote_currency,
                        rate_timestamp,
                        rate,
                        source,
                        fetched_at
                    FROM fx_rates
                    WHERE base_currency = ?
                      AND quote_currency = ?
                      AND rate_timestamp >= ?
                      AND rate_timestamp <= ?
                    ORDER BY
                        rate_timestamp,
                        fetched_at,
                        source,
                        id
                    """,
                    fxRateMapper(),
                    baseCurrency.value(),
                    quoteCurrency.value(),
                    timestamp(from),
                    timestamp(to)
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudieron consultar los tipos de cambio.",
                    exception
            );
        }
    }

    private void validateCurrencies(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency
    ) {
        if (baseCurrency == null
                || quoteCurrency == null) {
            throw new IllegalArgumentException(
                    "Las monedas del FX son obligatorias."
            );
        }

        if (baseCurrency.equals(
                quoteCurrency
        )) {
            throw new IllegalArgumentException(
                    "Las monedas del FX deben ser distintas."
            );
        }
    }

    private RowMapper<FxRate> fxRateMapper() {
        return this::mapFxRate;
    }

    private FxRate mapFxRate(
            ResultSet rs,
            int rowNum
    ) throws SQLException {
        return new FxRate(
                uuid(
                        rs,
                        "id"
                ),
                CurrencyCode.of(
                        rs.getString(
                                "base_currency"
                        )
                ),
                CurrencyCode.of(
                        rs.getString(
                                "quote_currency"
                        )
                ),
                instant(
                        rs,
                        "rate_timestamp"
                ),
                rs.getBigDecimal(
                        "rate"
                ),
                rs.getString(
                        "source"
                ),
                instant(
                        rs,
                        "fetched_at"
                )
        );
    }

    private Timestamp timestamp(
            Instant value
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria."
            );
        }

        return Timestamp.from(
                value
        );
    }

    private Instant instant(
            ResultSet rs,
            String column
    ) throws SQLException {
        Timestamp value =
                rs.getTimestamp(
                        column
                );

        return value == null
                ? null
                : value.toInstant();
    }

    private UUID uuid(
            ResultSet rs,
            String column
    ) throws SQLException {
        Object value =
                rs.getObject(
                        column
                );

        return value == null
                ? null
                : (UUID) value;
    }
}