package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityDetails;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public final class ActivityPostgresAdapter
        implements ActivityRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ActivityPostgresAdapter(
            JdbcTemplate jdbc,
            ObjectMapper objectMapper
    ) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public Activity save(
            Activity activity
    ) {
        String detailsJson =
                serializeDetails(
                        activity.getDetails()
                );

        jdbc.update(
                """
                INSERT INTO investment_activities (
                    id,
                    user_id,
                    type,
                    asset_reference_kind,
                    asset_reference_id,
                    occurred_at,
                    created_at,
                    modified_at,
                    ordering_key,
                    details,
                    comment,
                    linked_transaction_id
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?::jsonb,
                    ?,
                    ?
                )
                ON CONFLICT (id) DO UPDATE SET
                    occurred_at = EXCLUDED.occurred_at,
                    modified_at = EXCLUDED.modified_at,
                    ordering_key = EXCLUDED.ordering_key,
                    details = EXCLUDED.details,
                    comment = EXCLUDED.comment,
                    linked_transaction_id = EXCLUDED.linked_transaction_id
                """,
                activity.getId(),
                activity.getUserId(),
                activity.getType().name(),
                assetReferenceKind(
                        activity.getAssetReference()
                ),
                assetReferenceId(
                        activity.getAssetReference()
                ),
                timestamp(
                        activity.getOccurredAt()
                ),
                timestamp(
                        activity.getCreatedAt()
                ),
                timestamp(
                        activity.getModifiedAt()
                ),
                activity.getOrderingKey(),
                detailsJson,
                activity.getComment(),
                activity.getLinkedTransactionId()
        );

        return activity;
    }

    @Override
    public Optional<Activity> findByIdAndUserId(
            UUID activityId,
            UUID userId
    ) {
        if (activityId == null
                || userId == null) {
            return Optional.empty();
        }

        return one(
                """
                SELECT
                    id,
                    user_id,
                    type,
                    asset_reference_kind,
                    asset_reference_id,
                    occurred_at,
                    created_at,
                    modified_at,
                    ordering_key,
                    details,
                    comment,
                    linked_transaction_id
                FROM investment_activities
                WHERE id = ?
                  AND user_id = ?
                """,
                activityMapper(),
                activityId,
                userId
        );
    }

    @Override
    public List<Activity> findAllByUserId(
            UUID userId
    ) {
        if (userId == null) {
            return List.of();
        }

        return jdbc.query(
                """
                SELECT
                    id,
                    user_id,
                    type,
                    asset_reference_kind,
                    asset_reference_id,
                    occurred_at,
                    created_at,
                    modified_at,
                    ordering_key,
                    details,
                    comment,
                    linked_transaction_id
                FROM investment_activities
                WHERE user_id = ?
                ORDER BY
                    occurred_at,
                    ordering_key,
                    id
                """,
                activityMapper(),
                userId
        );
    }

    @Override
    public List<Activity> findByUserIdAndFilters(
            UUID userId,
            AssetReference assetReference,
            Instant from,
            Instant to
    ) {
        if (userId == null) {
            return List.of();
        }

        StringBuilder sql =
                new StringBuilder(
                        """
                        SELECT
                            id,
                            user_id,
                            type,
                            asset_reference_kind,
                            asset_reference_id,
                            occurred_at,
                            created_at,
                            modified_at,
                            ordering_key,
                            details,
                            comment,
                            linked_transaction_id
                        FROM investment_activities
                        WHERE user_id = ?
                        """
                );

        List<Object> parameters =
                new ArrayList<>();

        parameters.add(userId);

        if (assetReference != null) {
            sql.append(
                    """
                      AND asset_reference_kind = ?
                      AND asset_reference_id = ?
                    """
            );

            parameters.add(
                    assetReference.kind().name()
            );

            parameters.add(
                    assetReference.id()
            );
        }

        if (from != null) {
            sql.append(
                    """
                      AND occurred_at >= ?
                    """
            );

            parameters.add(
                    timestamp(from)
            );
        }

        if (to != null) {
            sql.append(
                    """
                      AND occurred_at <= ?
                    """
            );

            parameters.add(
                    timestamp(to)
            );
        }

        sql.append(
                """
                ORDER BY
                    occurred_at,
                    ordering_key,
                    id
                """
        );

        return jdbc.query(
                sql.toString(),
                activityMapper(),
                parameters.toArray()
        );
    }

    private RowMapper<Activity> activityMapper() {
        return (
                ResultSet rs,
                int rowNum
        ) -> mapActivity(rs);
    }

    private Activity mapActivity(
            ResultSet rs
    ) throws SQLException {
        UUID id =
                uuid(
                        rs,
                        "id"
                );

        UUID userId =
                uuid(
                        rs,
                        "user_id"
                );

        ActivityType type =
                ActivityType.valueOf(
                        rs.getString("type")
                );

        AssetReference assetReference =
                readAssetReference(
                        rs.getString(
                                "asset_reference_kind"
                        ),
                        uuid(
                                rs,
                                "asset_reference_id"
                        )
                );

        Instant occurredAt =
                instant(
                        rs,
                        "occurred_at"
                );

        Instant createdAt =
                instant(
                        rs,
                        "created_at"
                );

        Instant modifiedAt =
                instant(
                        rs,
                        "modified_at"
                );

        long orderingKey =
                rs.getLong(
                        "ordering_key"
                );

        String detailsJson =
                rs.getString(
                        "details"
                );

        ActivityDetails details =
                deserializeDetails(
                        type,
                        detailsJson
                );

        String comment =
                rs.getString(
                        "comment"
                );

        UUID linkedTransactionId =
                uuid(
                        rs,
                        "linked_transaction_id"
                );

        return Activity.reconstitute(
                id,
                userId,
                type,
                assetReference,
                occurredAt,
                createdAt,
                modifiedAt,
                orderingKey,
                details,
                comment,
                linkedTransactionId
        );
    }

    private String serializeDetails(
            ActivityDetails details
    ) {
        try {
            return objectMapper.writeValueAsString(
                    details
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "No se pudieron serializar los detalles de la Activity.",
                    exception
            );
        }
    }

    private ActivityDetails deserializeDetails(
            ActivityType type,
            String json
    ) {
        if (json == null
                || json.isBlank()) {
            throw new IllegalStateException(
                    "Una Activity debe tener details."
            );
        }

        try {
            JsonNode node =
                    objectMapper.readTree(
                            json
                    );

            Class<? extends ActivityDetails> detailsType =
                    detailsType(
                            type,
                            node
                    );

            return objectMapper.treeToValue(
                    node,
                    detailsType
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "No se pudieron deserializar los detalles "
                            + "de la Activity de tipo "
                            + type
                            + ".",
                    exception
            );
        }
    }

    private Class<? extends ActivityDetails> detailsType(
            ActivityType type,
            JsonNode node
    ) {
        return switch (type) {

            case BUY, SELL ->
                    com.puntomartinez.millete.investments.domain.model.TradeActivityDetails.class;

            case DIVIDEND -> {
                if (node.has("quantity")
                        && node.has("referenceUnitPrice")) {

                    yield com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails.class;
                }

                yield com.puntomartinez.millete.investments.domain.model.CashActivityDetails.class;
            }

            case DEPOSIT, WITHDRAW, OPENING_CASH ->
                    com.puntomartinez.millete.investments.domain.model.CashActivityDetails.class;

            case SPLIT ->
                    com.puntomartinez.millete.investments.domain.model.SplitActivityDetails.class;

            case EXCHANGE ->
                    com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails.class;

            case OPENING_POSITION ->
                    com.puntomartinez.millete.investments.domain.model.OpeningPositionDetails.class;
        };
    }

    private AssetReference readAssetReference(
            String kind,
            UUID id
    ) {
        if (kind == null
                || id == null) {
            return null;
        }

        return switch (
                AssetReferenceKind.valueOf(
                        kind
                )
        ) {
            case SHARED ->
                    AssetReference.shared(
                            id
                    );

            case USER ->
                    AssetReference.user(
                            id
                    );
        };
    }

    private String assetReferenceKind(
            AssetReference reference
    ) {
        return reference == null
                ? null
                : reference.kind().name();
    }

    private UUID assetReferenceId(
            AssetReference reference
    ) {
        return reference == null
                ? null
                : reference.id();
    }

    private Optional<Activity> one(
            String sql,
            RowMapper<Activity> mapper,
            Object... parameters
    ) {
        try {
            List<Activity> result =
                    jdbc.query(
                            sql,
                            mapper,
                            parameters
                    );

            return result
                    .stream()
                    .findFirst();

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo consultar una Activity.",
                    exception
            );
        }
    }

    private Timestamp timestamp(
            Instant value
    ) {
        return value == null
                ? null
                : Timestamp.from(value);
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