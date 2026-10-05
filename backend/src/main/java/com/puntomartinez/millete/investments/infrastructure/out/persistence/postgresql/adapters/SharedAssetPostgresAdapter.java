package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public final class SharedAssetPostgresAdapter
        implements SharedAssetRepository {

    private final JdbcTemplate jdbc;

    public SharedAssetPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public List<SharedAsset> findAll() {
        return jdbc.query(
                """
                SELECT
                    id,
                    stable_catalog_id,
                    name,
                    symbol,
                    type,
                    sector_code,
                    sector_display_name,
                    sector_custom,
                    currency,
                    created_at,
                    modified_at
                FROM shared_assets
                ORDER BY
                    name,
                    id
                """,
                sharedAssetMapper()
        );
    }

    @Override
    public Optional<SharedAsset> findById(
            UUID sharedAssetId
    ) {
        if (sharedAssetId == null) {
            return Optional.empty();
        }

        return jdbc.query(
                """
                SELECT
                    id,
                    stable_catalog_id,
                    name,
                    symbol,
                    type,
                    sector_code,
                    sector_display_name,
                    sector_custom,
                    currency,
                    created_at,
                    modified_at
                FROM shared_assets
                WHERE id = ?
                """,
                sharedAssetMapper(),
                sharedAssetId
        )
        .stream()
        .findFirst();
    }

    @Override
    public Optional<SharedAsset> findByStableCatalogId(
            String stableCatalogId
    ) {
        if (stableCatalogId == null
                || stableCatalogId.isBlank()) {
            return Optional.empty();
        }

        return jdbc.query(
                """
                SELECT
                    id,
                    stable_catalog_id,
                    name,
                    symbol,
                    type,
                    sector_code,
                    sector_display_name,
                    sector_custom,
                    currency,
                    created_at,
                    modified_at
                FROM shared_assets
                WHERE stable_catalog_id = ?
                """,
                sharedAssetMapper(),
                stableCatalogId.trim()
        )
        .stream()
        .findFirst();
    }

    @Override
    public List<SharedAsset> search(
            String search
    ) {
        if (search == null
                || search.isBlank()) {
            return findAll();
        }

        String pattern =
                "%"
                        + search.trim().toLowerCase()
                        + "%";

        return jdbc.query(
                """
                SELECT
                    id,
                    stable_catalog_id,
                    name,
                    symbol,
                    type,
                    sector_code,
                    sector_display_name,
                    sector_custom,
                    currency,
                    created_at,
                    modified_at
                FROM shared_assets
                WHERE LOWER(name) LIKE ?
                   OR LOWER(COALESCE(symbol, '')) LIKE ?
                   OR LOWER(stable_catalog_id) LIKE ?
                ORDER BY
                    name,
                    id
                """,
                sharedAssetMapper(),
                pattern,
                pattern,
                pattern
        );
    }

    private RowMapper<SharedAsset> sharedAssetMapper() {
        return this::mapSharedAsset;
    }

    private SharedAsset mapSharedAsset(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        UUID id =
                uuid(
                        rs,
                        "id"
                );

        String stableCatalogId =
                rs.getString(
                        "stable_catalog_id"
                );

        String name =
                rs.getString(
                        "name"
                );

        String symbol =
                rs.getString(
                        "symbol"
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

        return SharedAsset.reconstitute(
                id,
                stableCatalogId,
                name,
                symbol,
                type,
                sector,
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
                    "Un SharedAsset persistido debe tener un sector."
            );
        }

        return new AssetSector(
                code,
                displayName,
                custom
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