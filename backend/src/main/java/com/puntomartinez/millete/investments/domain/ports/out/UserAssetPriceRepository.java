package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserAssetPriceRepository {

    UserAssetPrice save(UserAssetPrice userAssetPrice);

    Optional<UserAssetPrice> findLatestAt(
            UUID userAssetId,
            Instant at
    );
}