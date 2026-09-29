package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.SharedAsset;

import java.util.UUID;

public interface GetSharedAssetUseCase {

    SharedAsset getById(UUID sharedAssetId);
}