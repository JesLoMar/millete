package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.AssetSector;

import java.util.List;
import java.util.Optional;

public interface AssetSectorCatalogPort {

    Optional<AssetSector> findCommonByReference(String reference);

    List<AssetSector> findAllCommon();
}