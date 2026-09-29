package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.ports.in.GetSharedAssetUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListAssetSectorsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.SearchSharedAssetsUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.AssetSectorCatalogPort;
import com.puntomartinez.millete.investments.domain.ports.out.SharedAssetRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SharedAssetCatalogService implements
        SearchSharedAssetsUseCase,
        GetSharedAssetUseCase,
        ListAssetSectorsUseCase {

    private final SharedAssetRepository sharedAssets;
    private final AssetSectorCatalogPort sectorCatalog;

    public SharedAssetCatalogService(
            SharedAssetRepository sharedAssets,
            AssetSectorCatalogPort sectorCatalog
    ) {
        this.sharedAssets = sharedAssets;
        this.sectorCatalog = sectorCatalog;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedAsset> search(String search) {
        return sharedAssets.search(normalizeSearch(search));
    }

    @Override
    @Transactional(readOnly = true)
    public SharedAsset getById(UUID sharedAssetId) {
        return sharedAssets.findById(sharedAssetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "SharedAsset no encontrado"
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetSector> list() {
        return sectorCatalog.findAllCommon();
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }
}