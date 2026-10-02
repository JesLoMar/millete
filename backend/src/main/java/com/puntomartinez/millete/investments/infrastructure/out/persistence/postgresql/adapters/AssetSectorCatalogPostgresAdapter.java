package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.ports.out.AssetSectorCatalogPort;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public final class AssetSectorCatalogPostgresAdapter
        implements AssetSectorCatalogPort {

    private final JdbcTemplate jdbc;

    public AssetSectorCatalogPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<AssetSector> findCommonByReference(
            String reference
    ) {
        if (reference == null
                || reference.isBlank()) {
            return Optional.empty();
        }

        try {
            return jdbc.query(
                    """
                    SELECT
                        code,
                        display_name
                    FROM asset_sectors
                    WHERE active = true
                      AND code = ?
                    LIMIT 1
                    """,
                    (rs, rowNum) ->
                            AssetSector.common(
                                    rs.getString("code"),
                                    rs.getString("display_name")
                            ),
                    reference.trim().toUpperCase()
            )
            .stream()
            .findFirst();

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo consultar el sector de catálogo.",
                    exception
            );
        }
    }

    @Override
    public List<AssetSector> findAllCommon() {
        try {
            return jdbc.query(
                    """
                    SELECT
                        code,
                        display_name
                    FROM asset_sectors
                    WHERE active = true
                    ORDER BY display_name, code
                    """,
                    (rs, rowNum) ->
                            AssetSector.common(
                                    rs.getString("code"),
                                    rs.getString("display_name")
                            )
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudieron consultar los sectores de catálogo.",
                    exception
            );
        }
    }
}