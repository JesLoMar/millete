package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.out.UserAssetPriceRepository;
import org.springframework.dao.DataAccessException;
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
public final class UserAssetPricePostgresAdapter
        implements UserAssetPriceRepository {

    private final JdbcTemplate jdbc;

    public UserAssetPricePostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public UserAssetPrice save(
            UserAssetPrice userAssetPrice
    ) {
        if (userAssetPrice == null) {
            throw new IllegalArgumentException(
                    "El precio del UserAsset es obligatorio."
            );
        }

        try {
            jdbc.update(
                    """
                    INSERT INTO user_asset_prices (
                        id,
                        user_asset_id,
                        unit_price,
                        currency,
                        price_timestamp
                    )
                    VALUES (
                        ?,
                        ?,
                        ?,
                        ?,
                        ?
                    )
                    """,
                    userAssetPrice.getId(),
                    userAssetPrice.getUserAssetId(),
                    userAssetPrice.getUnitPrice().amount(),
                    userAssetPrice.getUnitPrice().currency().value(),
                    timestamp(
                            userAssetPrice.getTimestamp()
                    )
            );

            return userAssetPrice;

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo guardar el precio del UserAsset.",
                    exception
            );
        }
    }

    @Override
    public Optional<UserAssetPrice> findLatestAt(
            UUID userAssetId,
            Instant at
    ) {
        if (userAssetId == null
                || at == null) {
            return Optional.empty();
        }

        try {
            return jdbc.query(
                    """
                    SELECT
                        p.id,
                        p.user_asset_id,
                        p.unit_price,
                        p.currency,
                        p.price_timestamp
                    FROM user_asset_prices p
                    INNER JOIN user_assets a
                        ON a.id = p.user_asset_id
                    WHERE p.user_asset_id = ?
                      AND p.price_timestamp <= ?
                    ORDER BY
                        p.price_timestamp DESC,
                        p.id DESC
                    LIMIT 1
                    """,
                    userAssetPriceMapper(),
                    userAssetId,
                    timestamp(at)
            )
            .stream()
            .findFirst();

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudo consultar el último precio "
                            + "del UserAsset.",
                    exception
            );
        }
    }

    @Override
    public List<UserAssetPrice> findAllByUserId(
            UUID userId
    ) {
        if (userId == null) {
            return List.of();
        }

        try {
            return jdbc.query(
                    """
                    SELECT
                        p.id,
                        p.user_asset_id,
                        p.unit_price,
                        p.currency,
                        p.price_timestamp
                    FROM user_asset_prices p
                    INNER JOIN user_assets a
                        ON a.id = p.user_asset_id
                    WHERE a.user_id = ?
                    ORDER BY
                        p.price_timestamp,
                        p.id
                    """,
                    userAssetPriceMapper(),
                    userId
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudieron consultar los precios "
                            + "de los UserAssets.",
                    exception
            );
        }
    }

    private RowMapper<UserAssetPrice> userAssetPriceMapper() {
        return this::mapUserAssetPrice;
    }

    private UserAssetPrice mapUserAssetPrice(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        UUID id =
                uuid(
                        rs,
                        "id"
                );

        UUID userAssetId =
                uuid(
                        rs,
                        "user_asset_id"
                );

        Money unitPrice =
                new Money(
                        rs.getBigDecimal(
                                "unit_price"
                        ),
                        CurrencyCode.of(
                                rs.getString(
                                        "currency"
                                )
                        )
                );

        Instant timestamp =
                instant(
                        rs,
                        "price_timestamp"
                );

        return UserAssetPrice.reconstitute(
                id,
                userAssetId,
                unitPrice,
                timestamp
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