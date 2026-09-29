package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetOrigin;

import java.math.BigDecimal;
import java.util.UUID;

public interface RegisterUserAssetUseCase {

    UserAsset register(UUID userId, RegisterUserAssetCommand command);

    record RegisterUserAssetCommand(
            String name,
            AssetType type,
            String sector,
            UserAssetOrigin origin,
            BigDecimal initialPrice
    ) {
    }
}