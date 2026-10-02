package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.AssetPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaAssetPriceRepository
        extends JpaRepository<AssetPriceEntity, UUID> {

    @Query("""
        SELECT p
        FROM AssetPriceEntity p
        WHERE p.sharedAssetId = :sharedAssetId
          AND p.timestamp <= :at
          AND (
              p.close IS NOT NULL
              OR p.adjustedClose IS NOT NULL
          )
        ORDER BY
            p.timestamp DESC,
            p.fetchedAt DESC,
            p.source ASC,
            p.id DESC
    """)
    List<AssetPriceEntity> findLatestCandidates(
            @Param("sharedAssetId") UUID sharedAssetId,
            @Param("at") Instant at
    );

    List<AssetPriceEntity> findBySharedAssetIdAndTimestampBetweenOrderByTimestampAscFetchedAtAscSourceAscIdAsc(
            UUID sharedAssetId,
            Instant from,
            Instant to
    );

    @Modifying
    @Query(value = """
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
            :id,
            :sharedAssetId,
            :timestamp,
            :open,
            :high,
            :low,
            :close,
            :adjustedClose,
            :volume,
            :currency,
            :source,
            :fetchedAt
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
        """, nativeQuery = true)
    int upsert(
            @Param("id") UUID id,
            @Param("sharedAssetId") UUID sharedAssetId,
            @Param("timestamp") Instant timestamp,
            @Param("open") BigDecimal open,
            @Param("high") BigDecimal high,
            @Param("low") BigDecimal low,
            @Param("close") BigDecimal close,
            @Param("adjustedClose") BigDecimal adjustedClose,
            @Param("volume") BigDecimal volume,
            @Param("currency") String currency,
            @Param("source") String source,
            @Param("fetchedAt") Instant fetchedAt
    );
}