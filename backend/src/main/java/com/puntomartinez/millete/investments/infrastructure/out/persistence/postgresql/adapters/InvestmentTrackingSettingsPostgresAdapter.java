package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.ports.out.InvestmentTrackingSettingsRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public final class InvestmentTrackingSettingsPostgresAdapter
        implements InvestmentTrackingSettingsRepository {

    private final JdbcTemplate jdbc;

    public InvestmentTrackingSettingsPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Instant> findTrackingStartAt(
            UUID userId
    ) {
        if (userId == null) {
            return Optional.empty();
        }

        try {
            List<Instant> result =
                    jdbc.query(
                            """
                            SELECT tracking_start_at
                            FROM investment_tracking_settings
                            WHERE user_id = ?
                            LIMIT 1
                            """,
                            (rs, rowNum) -> {
                                Timestamp value =
                                        rs.getTimestamp(
                                                "tracking_start_at"
                                        );

                                return value == null
                                        ? null
                                        : value.toInstant();
                            },
                            userId
                    );

            return result.stream()
                    .findFirst();

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo consultar la configuración "
                            + "de tracking de Investments.",
                    exception
            );
        }
    }

    @Override
    public void saveTrackingStartAt(
            UUID userId,
            Instant trackingStartAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId es obligatorio."
            );
        }

        if (trackingStartAt == null) {
            throw new IllegalArgumentException(
                    "trackingStartAt es obligatorio."
            );
        }

        try {
            jdbc.update(
                    """
                    INSERT INTO investment_tracking_settings (
                        user_id,
                        tracking_start_at
                    )
                    VALUES (
                        ?,
                        ?
                    )
                    ON CONFLICT (user_id)
                    DO UPDATE SET
                        tracking_start_at =
                            EXCLUDED.tracking_start_at
                    """,
                    userId,
                    Timestamp.from(
                            trackingStartAt
                    )
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo guardar la configuración "
                            + "de tracking de Investments.",
                    exception
            );
        }
    }
}