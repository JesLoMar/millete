package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Asset;
import com.puntomartinez.millete.investments.domain.model.AssetSector;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository {
    Asset save(Asset asset);
    Optional<Asset> findAssetByIdAndUserId(UUID id, UUID userId);
    List<Asset> findAssetsByUserId(UUID userId, boolean includeInactive);
    boolean sectorExists(UUID sectorId);
    List<AssetSector> findActiveSectors();
}
