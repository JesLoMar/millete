package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
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
public final class AssetPricePostgresAdapter
        implements AssetPriceRepository {

    private final JdbcTemplate jdbc;

    public AssetPricePostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public void saveAll(
            List<AssetPrice> prices
    ) {
        if (prices == null
                || prices.isEmpty()) {
            return;
        }

        try {
            jdbc.batchUpdate(
                    """
                    INSERT INTO asset_prices (
                        id,
                        shared_asset_id,
                        price_timestamp,
                        open,
                        high,
                        low,
                        close,
                        adjusted_close,
                        volume,
                        currency,
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
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?
                    )
                    ON CONFLICT (
                        shared_asset_id,
                        price_timestamp,
                        source
                    )
                    DO UPDATE SET
                        open = EXCLUDED.open,
                        high = EXCLUDED.high,
                        low = EXCLUDED.low,
                        close = EXCLUDED.close,
                        adjusted_close = EXCLUDED.adjusted_close,
                        volume = EXCLUDED.volume,
                        currency = EXCLUDED.currency,
                        fetched_at = EXCLUDED.fetched_at
                    """,
                    prices,
                    prices.size(),
                    (
                            PreparedStatement statement,
                            AssetPrice price
                    ) -> {
                        statement.setObject(
                                1,
                                price.id()
                        );

                        statement.setObject(
                                2,
                                price.sharedAssetId()
                        );

                        statement.setTimestamp(
                                3,
                                timestamp(
                                        price.timestamp()
                                )
                        );

                        statement.setBigDecimal(
                                4,
                                price.open()
                        );

                        statement.setBigDecimal(
                                5,
                                price.high()
                        );

                        statement.setBigDecimal(
                                6,
                                price.low()
                        );

                        statement.setBigDecimal(
                                7,
                                price.close()
                        );

                        statement.setBigDecimal(
                                8,
                                price.adjustedClose()
                        );

                        statement.setBigDecimal(
                                9,
                                price.volume()
                        );

                        statement.setString(
                                10,
                                price.currency().value()
                        );

                        statement.setString(
                                11,
                                price.source()
                        );

                        statement.setTimestamp(
                                12,
                                timestamp(
                                        price.fetchedAt()
                                )
                        );
                    }
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudieron guardar los precios de mercado.",
                    exception
            );
        }
    }

    @Override
    public Optional<AssetPrice> findLatestAt(
            UUID sharedAssetId,
            Instant at
    ) {
        if (sharedAssetId == null
                || at == null) {
            return Optional.empty();
        }

        try {
            return jdbc.query(
                    """
                    SELECT
                        id,
                        shared_asset_id,
                        price_timestamp,
                        open,
                        high,
                        low,
                        close,
                        adjusted_close,
                        volume,
                        currency,
                        source,
                        fetched_at
                    FROM asset_prices
                    WHERE shared_asset_id = ?
                      AND price_timestamp <= ?
                      AND (
                          close IS NOT NULL
                          OR adjusted_close IS NOT NULL
                      )
                    ORDER BY
                        price_timestamp DESC,
                        fetched_at DESC,
                        source,
                        id DESC
                    LIMIT 1
                    """,
                    assetPriceMapper(),
                    sharedAssetId,
                    timestamp(at)
            )
            .stream()
            .findFirst();

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo consultar el último precio "
                            + "del SharedAsset.",
                    exception
            );
        }
    }

    @Override
    public List<AssetPrice> findBySharedAssetIdAndTimestampBetween(
            UUID sharedAssetId,
            Instant from,
            Instant to
    ) {
        if (sharedAssetId == null) {
            return List.of();
        }

        if (from == null
                || to == null
                || from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "El rango temporal de precios no es válido."
            );
        }

        try {
            return jdbc.query(
                    """
                    SELECT
                        id,
                        shared_asset_id,
                        price_timestamp,
                        open,
                        high,
                        low,
                        close,
                        adjusted_close,
                        volume,
                        currency,
                        source,
                        fetched_at
                    FROM asset_prices
                    WHERE shared_asset_id = ?
                      AND price_timestamp >= ?
                      AND price_timestamp <= ?
                    ORDER BY
                        price_timestamp,
                        fetched_at,
                        source,
                        id
                    """,
                    assetPriceMapper(),
                    sharedAssetId,
                    timestamp(from),
                    timestamp(to)
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudieron consultar los precios del SharedAsset.",
                    exception
            );
        }
    }

    private RowMapper<AssetPrice> assetPriceMapper() {
        return this::mapAssetPrice;
    }

    private AssetPrice mapAssetPrice(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        UUID id =
                uuid(
                        rs,
                        "id"
                );

        UUID sharedAssetId =
                uuid(
                        rs,
                        "shared_asset_id"
                );

        Instant timestamp =
                instant(
                        rs,
                        "price_timestamp"
                );

        return new AssetPrice(
                id,
                sharedAssetId,
                timestamp,
                rs.getBigDecimal("open"),
                rs.getBigDecimal("high"),
                rs.getBigDecimal("low"),
                rs.getBigDecimal("close"),
                rs.getBigDecimal("adjusted_close"),
                rs.getBigDecimal("volume"),
                CurrencyCode.of(
                        rs.getString("currency")
                ),
                rs.getString("source"),
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