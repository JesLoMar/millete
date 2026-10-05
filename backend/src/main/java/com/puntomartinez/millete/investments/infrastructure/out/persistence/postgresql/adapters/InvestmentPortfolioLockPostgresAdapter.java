package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.ports.out.InvestmentPortfolioLockPort;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public final class InvestmentPortfolioLockPostgresAdapter
        implements InvestmentPortfolioLockPort {

    private static final String LOCK_PREFIX =
            "investment-user:";

    private final JdbcTemplate jdbc;

    public InvestmentPortfolioLockPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public void lock(
            UUID userId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId es obligatorio."
            );
        }

        try {
            jdbc.queryForObject(
                    """
                    SELECT pg_advisory_xact_lock(
                        hashtextextended(?, 0)
                    )
                    """,
                    Object.class,
                    LOCK_PREFIX + userId
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo adquirir el lock del portfolio "
                            + "de Investments.",
                    exception
            );
        }
    }
}