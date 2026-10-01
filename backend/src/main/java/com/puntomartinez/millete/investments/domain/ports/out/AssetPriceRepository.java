package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetPriceRepository {

    void saveAll(List<AssetPrice> prices);

    Optional<AssetPrice> findLatestAt(
            UUID sharedAssetId,
            Instant at
    );

    List<AssetPrice> findBySharedAssetIdAndTimestampBetween(
            UUID sharedAssetId,
            Instant from,
            Instant to
    );
}