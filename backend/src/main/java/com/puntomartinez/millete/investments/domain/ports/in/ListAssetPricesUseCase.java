package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ListAssetPricesUseCase {

    List<AssetPrice> list(
            UUID sharedAssetId,
            Instant from,
            Instant to
    );
}