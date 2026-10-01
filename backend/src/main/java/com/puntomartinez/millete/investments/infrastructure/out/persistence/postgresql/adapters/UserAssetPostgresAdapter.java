package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.AssetOrigin;
import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetRepository;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
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
public final class UserAssetPostgresAdapter
        implements UserAssetRepository {

    private final JdbcTemplate jdbc;

    public UserAssetPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public UserAsset save(
            UserAsset userAsset
    ) {
        if (userAsset == null) {
            throw new IllegalArgumentException(
                    "El UserAsset es obligatorio."
            );
        }

        try {
            jdbc.update(
                    """
                    INSERT INTO user_assets (
                        id,
                        user_id,
                        name,
                        type,
                        sector_code,
                        sector_display_name,
                        sector_custom,
                        origin,
                        currency,
                        created_at,
                        modified_at
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
                        ?,
                        ?
                    )
                    ON CONFLICT (id) DO UPDATE SET
                        name = EXCLUDED.name,
                        sector_code = EXCLUDED.sector_code,
                        sector_display_name = EXCLUDED.sector_display_name,
                        sector_custom = EXCLUDED.sector_custom,
                        modified_at = EXCLUDED.modified_at
                    """,
                    userAsset.getId(),
                    userAsset.getUserId(),
                    userAsset.getName(),
                    userAsset.getType().name(),
                    sectorCode(
                            userAsset.getSector()
                    ),
                    sectorDisplayName(
                            userAsset.getSector()
                    ),
                    sectorCustom(
                            userAsset.getSector()
                    ),
                    userAsset.getOrigin().name(),
                    userAsset.getCurrency().value(),
                    timestamp(
                            userAsset.getCreatedAt()
                    ),
                    timestamp(
                            userAsset.getModifiedAt()
                    )
            );

            return userAsset;

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo persistir el UserAsset.",
                    exception
            );
        }
    }

    @Override
    public Optional<UserAsset> findByIdAndUserId(
            UUID userAssetId,
            UUID userId
    ) {
        if (userAssetId == null
                || userId == null) {
            return Optional.empty();
        }

        try {
            return jdbc.query(
                    """
                    SELECT
                        id,
                        user_id,
                        name,
                        type,
                        sector_code,
                        sector_display_name,
                        sector_custom,
                        origin,
                        currency,
                        created_at,
                        modified_at
                    FROM user_assets
                    WHERE id = ?
                      AND user_id = ?
                    """,
                    userAssetMapper(),
                    userAssetId,
                    userId
            )
            .stream()
            .findFirst();

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo consultar el UserAsset.",
                    exception
            );
        }
    }

    @Override
    public List<UserAsset> findAllByUserId(
            UUID userId,
            String search
    ) {
        if (userId == null) {
            return List.of();
        }

        try {
            if (search == null
                    || search.isBlank()) {

                return jdbc.query(
                        """
                        SELECT
                            id,
                            user_id,
                            name,
                            type,
                            sector_code,
                            sector_display_name,
                            sector_custom,
                            origin,
                            currency,
                            created_at,
                            modified_at
                        FROM user_assets
                        WHERE user_id = ?
                        ORDER BY
                            name,
                            id
                        """,
                        userAssetMapper(),
                        userId
                );
            }

            String pattern =
                    "%"
                            + search.trim().toLowerCase()
                            + "%";

            return jdbc.query(
                    """
                    SELECT
                        id,
                        user_id,
                        name,
                        type,
                        sector_code,
                        sector_display_name,
                        sector_custom,
                        origin,
                        currency,
                        created_at,
                        modified_at
                    FROM user_assets
                    WHERE user_id = ?
                      AND LOWER(name) LIKE ?
                    ORDER BY
                        name,
                        id
                    """,
                    userAssetMapper(),
                    userId,
                    pattern
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudieron consultar los UserAssets.",
                    exception
            );
        }
    }

    private RowMapper<UserAsset> userAssetMapper() {
        return this::mapUserAsset;
    }

    private UserAsset mapUserAsset(
            ResultSet rs,
            int rowNum
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

        String name =
                rs.getString(
                        "name"
                );

        AssetType type =
                AssetType.valueOf(
                        rs.getString(
                                "type"
                        )
                );

        AssetSector sector =
                mapSector(
                        rs
                );

        AssetOrigin origin =
                AssetOrigin.valueOf(
                        rs.getString(
                                "origin"
                        )
                );

        CurrencyCode currency =
                CurrencyCode.of(
                        rs.getString(
                                "currency"
                        )
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

        return UserAsset.reconstitute(
                id,
                userId,
                name,
                type,
                sector,
                origin,
                currency,
                createdAt,
                modifiedAt
        );
    }

    private AssetSector mapSector(
            ResultSet rs
    ) throws SQLException {

        String code =
                rs.getString(
                        "sector_code"
                );

        String displayName =
                rs.getString(
                        "sector_display_name"
                );

        boolean custom =
                rs.getBoolean(
                        "sector_custom"
                );

        if (rs.wasNull()) {
            custom = false;
        }

        if (code == null
                && displayName == null) {
            throw new IllegalStateException(
                    "Un UserAsset persistido debe tener un sector."
            );
        }

        return new AssetSector(
                code,
                displayName,
                custom
        );
    }

    private String sectorCode(
            AssetSector sector
    ) {
        return sector == null
                ? null
                : sector.code();
    }

    private String sectorDisplayName(
            AssetSector sector
    ) {
        return sector == null
                ? null
                : sector.displayName();
    }

    private boolean sectorCustom(
            AssetSector sector
    ) {
        return sector != null
                && sector.custom();
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