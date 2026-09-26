package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Holding;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldingRepository {
    Holding save(Holding holding);
    Optional<Holding> findHoldingByIdAndUserId(UUID id, UUID userId);
    List<Holding> findHoldingsByUserIdAndAssetId(UUID userId, UUID assetId);
    List<Holding> findHoldingsByUserId(UUID userId);
    List<Holding> findHoldingsIncludingSupersededByUserId(UUID userId);
    void markSuperseded(UUID holdingId, UUID userId);
}
