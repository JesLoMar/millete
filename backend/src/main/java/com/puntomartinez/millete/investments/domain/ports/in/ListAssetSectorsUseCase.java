package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetSector;

import java.util.List;

public interface ListAssetSectorsUseCase {

    List<AssetSector> list();
}