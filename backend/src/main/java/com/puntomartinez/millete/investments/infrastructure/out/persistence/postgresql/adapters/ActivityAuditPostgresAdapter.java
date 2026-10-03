package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.ActivityAudit;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityAuditRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public final class ActivityAuditPostgresAdapter
        implements ActivityAuditRepository {

    private final JdbcTemplate jdbc;

    public ActivityAuditPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public ActivityAudit save(
            ActivityAudit audit
    ) {
        if (audit == null) {
            throw new IllegalArgumentException(
                    "El audit de la Activity es obligatorio."
            );
        }

        jdbc.update(
                """
                INSERT INTO activity_audit (
                    id,
                    activity_id,
                    user_id,
                    before_data,
                    after_data,
                    reason,
                    changed_at
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?::jsonb,
                    ?::jsonb,
                    ?,
                    ?
                )
                """,
                audit.id(),
                audit.activityId(),
                audit.userId(),
                audit.beforeJson(),
                audit.afterJson(),
                audit.reason(),
                timestamp(
                        audit.changedAt()
                )
        );

        return audit;
    }

    @Override
    public List<ActivityAudit> findAllByUserId(
            UUID userId
    ) {
        if (userId == null) {
            return List.of();
        }

        return jdbc.query(
                """
                SELECT
                    id,
                    activity_id,
                    user_id,
                    before_data,
                    after_data,
                    reason,
                    changed_at
                FROM activity_audit
                WHERE user_id = ?
                ORDER BY
                    changed_at,
                    id
                """,
                this::mapAudit,
                userId
        );
    }

    private ActivityAudit mapAudit(
            ResultSet rs,
            int rowNum
    ) throws SQLException {
        return new ActivityAudit(
                uuid(
                        rs,
                        "id"
                ),
                uuid(
                        rs,
                        "activity_id"
                ),
                uuid(
                        rs,
                        "user_id"
                ),
                rs.getString(
                        "before_data"
                ),
                rs.getString(
                        "after_data"
                ),
                rs.getString(
                        "reason"
                ),
                instant(
                        rs,
                        "changed_at"
                )
        );
    }

    private Timestamp timestamp(
            Instant value
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "changedAt es obligatorio."
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