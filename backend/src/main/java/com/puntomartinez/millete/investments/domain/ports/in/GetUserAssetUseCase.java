package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.UserAsset;

import java.util.UUID;

public interface GetUserAssetUseCase {

    UserAsset getById(UUID userId, UUID userAssetId);
}