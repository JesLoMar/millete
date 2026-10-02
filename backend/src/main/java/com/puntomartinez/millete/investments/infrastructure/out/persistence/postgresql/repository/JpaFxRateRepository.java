package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.FxRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaFxRateRepository
        extends JpaRepository<FxRateEntity, UUID> {

    @Query("""
        SELECT f
        FROM FxRateEntity f
        WHERE f.baseCurrency = :baseCurrency
          AND f.quoteCurrency = :quoteCurrency
          AND f.timestamp <= :at
        ORDER BY
            f.timestamp DESC,
            f.fetchedAt DESC,
            f.source ASC,
            f.id DESC
    """)
    List<FxRateEntity> findLatestCandidates(
            @Param("baseCurrency") String baseCurrency,
            @Param("quoteCurrency") String quoteCurrency,
            @Param("at") Instant at
    );

    List<FxRateEntity>
    findByBaseCurrencyAndQuoteCurrencyAndTimestampBetweenOrderByTimestampAscFetchedAtAscSourceAscIdAsc(
            String baseCurrency,
            String quoteCurrency,
            Instant from,
            Instant to
    );

    @Modifying
    @Query(value = """
        INSERT INTO fx_rates (
            id,
            base_currency,
            quote_currency,
            rate_timestamp,
            rate,
            source,
            fetched_at
        )
        VALUES (
            :id,
            :baseCurrency,
            :quoteCurrency,
            :timestamp,
            :rate,
            :source,
            :fetchedAt
        )
        ON CONFLICT (
            base_currency,
            quote_currency,
            rate_timestamp,
            source
        )
        DO UPDATE SET
            rate = EXCLUDED.rate,
            fetched_at = EXCLUDED.fetched_at
        """, nativeQuery = true)
    int upsert(
            @Param("id") UUID id,
            @Param("baseCurrency") String baseCurrency,
            @Param("quoteCurrency") String quoteCurrency,
            @Param("timestamp") Instant timestamp,
            @Param("rate") BigDecimal rate,
            @Param("source") String source,
            @Param("fetchedAt") Instant fetchedAt
    );
}