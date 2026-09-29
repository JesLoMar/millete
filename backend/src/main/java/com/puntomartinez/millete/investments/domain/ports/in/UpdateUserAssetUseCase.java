package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.UserAsset;

import java.util.UUID;

public interface UpdateUserAssetUseCase {

    UserAsset update(UUID userId, UUID userAssetId, UpdateUserAssetCommand command);

    record UpdateUserAssetCommand(
            String name,
            String sector
    ) {
    }
}