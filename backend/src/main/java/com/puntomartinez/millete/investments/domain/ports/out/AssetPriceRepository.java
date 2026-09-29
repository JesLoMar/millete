package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AssetPriceRepository {

    Optional<AssetPrice> findLatestAt(
            UUID sharedAssetId,
            Instant at
    );
}