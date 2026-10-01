package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.SharedAsset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SharedAssetRepository {

    List<SharedAsset> findAll();

    Optional<SharedAsset> findById(UUID sharedAssetId);

    Optional<SharedAsset> findByStableCatalogId(
            String stableCatalogId
    );

    List<SharedAsset> search(String search);
}