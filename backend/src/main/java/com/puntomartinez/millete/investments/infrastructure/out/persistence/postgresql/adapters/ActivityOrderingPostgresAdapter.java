package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.ports.out.ActivityOrderingPort;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
public final class ActivityOrderingPostgresAdapter
        implements ActivityOrderingPort {

    private final JdbcTemplate jdbc;

    public ActivityOrderingPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public long nextOrderingKey(
            UUID userId,
            Instant occurredAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId es obligatorio."
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "occurredAt es obligatorio."
            );
        }

        try {
            Long next =
                    jdbc.queryForObject(
                            """
                            SELECT
                                COALESCE(
                                    MAX(ordering_key),
                                    0
                                ) + 1
                            FROM investment_activities
                            WHERE user_id = ?
                              AND occurred_at = ?
                            """,
                            Long.class,
                            userId,
                            Timestamp.from(
                                    occurredAt
                            )
                    );

            return next == null
                    ? 1L
                    : next;

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo obtener el siguiente orderingKey "
                            + "para la Activity.",
                    exception
            );
        }
    }
}