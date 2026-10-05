package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.ports.out.InvestmentHealthTargetPort;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public final class InvestmentHealthTargetPostgresAdapter
        implements InvestmentHealthTargetPort {

    private final JdbcTemplate jdbc;

    public InvestmentHealthTargetPostgresAdapter(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public List<UUID> findUserIdsToCheck() {
        try {
            return jdbc.query(
                    """
                    SELECT user_id
                    FROM investment_activities

                    UNION

                    SELECT user_id
                    FROM holdings

                    UNION

                    SELECT user_id
                    FROM user_assets

                    ORDER BY user_id
                    """,
                    (rs, rowNum) ->
                            rs.getObject(
                                    "user_id",
                                    UUID.class
                            )
            );

        } catch (DataAccessException exception) {
            throw new IllegalStateException(
                    "No se pudieron obtener los usuarios "
                            + "que deben pasar la comprobación de salud "
                            + "de inversiones.",
                    exception
            );
        }
    }
}