package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.ports.out.ActivityIdempotencyRepository;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityIdempotencyRepository.IdempotencyEntry;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public final class ActivityIdempotencyPostgresAdapter
        implements ActivityIdempotencyRepository {

    private final JdbcTemplate jdbc;

    public ActivityIdempotencyPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<IdempotencyEntry> findByUserIdAndKey(
            UUID userId,
            String key
    ) {
        if (userId == null
                || key == null
                || key.isBlank()) {
            return Optional.empty();
        }

        return jdbc.query(
                """
                SELECT
                    user_id,
                    idempotency_key,
                    request_hash,
                    activity_id,
                    created_at
                FROM investment_activity_requests
                WHERE user_id = ?
                  AND idempotency_key = ?
                """,
                this::mapEntry,
                userId,
                key
        )
        .stream()
        .findFirst();
    }

    @Override
    public void save(
            IdempotencyEntry entry
    ) {
        if (entry == null) {
            throw new IllegalArgumentException(
                    "La entrada de idempotencia es obligatoria."
            );
        }

        try {
            jdbc.update(
                    """
                    INSERT INTO investment_activity_requests (
                        user_id,
                        idempotency_key,
                        request_hash,
                        activity_id,
                        created_at
                    )
                    VALUES (
                        ?,
                        ?,
                        ?,
                        ?,
                        ?
                    )
                    """,
                    entry.userId(),
                    entry.key(),
                    entry.requestFingerprint(),
                    entry.activityId(),
                    timestamp(
                            entry.createdAt()
                    )
            );

        } catch (DuplicateKeyException exception) {
            throw new IllegalStateException(
                    "Ya existe una entrada de idempotencia para "
                            + "el usuario y la clave indicados.",
                    exception
            );
        }
    }

    private IdempotencyEntry mapEntry(
            ResultSet rs,
            int rowNum
    ) throws SQLException {
        return new IdempotencyEntry(
                uuid(
                        rs,
                        "user_id"
                ),
                rs.getString(
                        "idempotency_key"
                ),
                rs.getString(
                        "request_hash"
                ),
                uuid(
                        rs,
                        "activity_id"
                ),
                instant(
                        rs,
                        "created_at"
                )
        );
    }

    private Timestamp timestamp(
            Instant value
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "createdAt es obligatorio."
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